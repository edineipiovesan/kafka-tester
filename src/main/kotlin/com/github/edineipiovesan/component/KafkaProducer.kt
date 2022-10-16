package com.github.edineipiovesan.component

import com.github.edineipiovesan.common.Timezone.OFFSET_ZONE
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.Headers
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDateTime
import java.time.ZoneId.of
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

class KafkaProducer {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val productionRate = ConcurrentHashMap<String, Long>()

    lateinit var eventGenerator: () -> Pair<String?, GenericRecord>
    lateinit var headerGenerator: (record: GenericRecord) -> Headers
    lateinit var kafkaTemplate: KafkaTemplate<String?, GenericRecord>
    lateinit var topicName: String

    @PostConstruct
    fun postConstruct() {
        logger.info("${this::class.simpleName} initialized successfully. Scheduler will start soon...")
    }

    /***
     * Calculate fixedRate value dividing 1000 by desired TPS.
     * eg:
     * - 50 TPS -> 1000/50=20 -> fixedRate=20
     * - 100 TPS -> 1000/100=10 -> fixedRate=10
     * - 120 TPS -> 1000/120=8,33 -> fixedRate=8
     *
     * If fixedRate should be below 1, set multiplier in
     * repeat(Int) function.
     */
    @Scheduled(initialDelay = 100, fixedRate = 100, timeUnit = TimeUnit.MILLISECONDS)
    private fun scheduler() {
        repeat(1) { publish() }
    }

    private fun publish() {
        val (key, record) = eventGenerator()
        val producerRecord = ProducerRecord<String?, GenericRecord>(topicName, key, record)

        headerGenerator(record).forEach { producerRecord.headers().add(it) }

        kafkaTemplate.send(producerRecord).addCallback(
            {
                val now = now()
                var counter = productionRate[now] ?: 0
                productionRate[now] = ++counter
            },
            { logger.error("An error occurred while producing message; key=$key; record$record", it) }
        )
    }

    /**
     * Print statistics on console with 3 seconds delay
     * ensuring all producer data was flushed
     * - Produced amount by time
     */
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    private fun printStatistics() {
        val now = now(minusSeconds = 3)
        val producedMessages = productionRate[now] ?: 0

        logger.info("[Production statistics]:\t$producedMessages produced at $now")
    }

    /**
     * Print statistics on console before application stops.
     * - Produced amount
     * - Start time
     * - End time
     */
    @PreDestroy
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
