package com.github.edineipiovesan.component

import com.github.edineipiovesan.common.Timezone.OFFSET_ZONE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId.of
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class KafkaProducerSingleton {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val productionRate = ConcurrentHashMap<String, Long>()
    private val scope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    fun scheduleProducer(tps: Long, initialDelay: Long = 0) {
        flow {
            delay(initialDelay)
            printStatistics()
            val rate = 1000.div(tps)
            logger.info("producing $tps TPS!; Using Rate: $rate")
            while (RequestSingleton.isOn()) {
                delay(rate)
                publish()
            }
            emit(true)
        }
            .onCompletion { preDestroy() }
            .launchIn(scope)
    }


    private fun publish() {
        val (_, request, record, headers, kafkaTemplate) = RequestSingleton.producer!!

        val producerRecord = ProducerRecord<String?, GenericRecord>(request.topic, request.key, record)

        headers.forEach { producerRecord.headers().add(it) }

        kafkaTemplate.send(producerRecord).addCallback(
            {
                val now = now()
                var counter = productionRate[now] ?: 0
                productionRate[now] = ++counter
            },
            { logger.error("An error occurred while producing message; key=${request.key}; record$record", it) }
        )
    }

    /**
     * Print statistics on console with 3 seconds delay
     * ensuring all producer data was flushed
     * - Produced amount by time
     */
    private fun printStatistics() {
        val statisticsScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

        flow {
            while (RequestSingleton.isOn()) {
                delay(1000)
                val now = now(minusSeconds = 3)
                val producedMessages = productionRate[now] ?: 0
                logger.info("[Production statistics]:\t$producedMessages produced at $now")
            }

            emit(Unit)
        }.launchIn(statisticsScope)

    }

    /**
     * Print statistics on console before application stops.
     * - Produced amount
     * - Start time
     * - End time
     */
    fun preDestroy() {
        val amount = if (productionRate.isEmpty()) 0 else productionRate.values.reduce { acc, current -> acc + current }
        val started = productionRate.keys.minOrNull()
        val ended = productionRate.keys.maxOrNull()

        logger.info("[Production statistics]:\t $amount was produced between $started and $ended")
    }

    private fun now(minusSeconds: Long = 0): String {
        return LocalDateTime.now(of(OFFSET_ZONE))
            .minusSeconds(minusSeconds)
            .truncatedTo(ChronoUnit.SECONDS)
            .toString()
    }
}
