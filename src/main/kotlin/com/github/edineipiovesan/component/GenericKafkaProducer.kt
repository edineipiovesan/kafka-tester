package com.github.edineipiovesan.component

import com.github.edineipiovesan.common.KafkaHeaderBuilder.generate
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import org.springframework.util.concurrent.ListenableFuture

@Component
class GenericKafkaProducer {

    private val logger: Logger = getLogger(this::class.java)

    fun send(
        topic: String,
        key: String? = null,
        record: GenericRecord,
        headers: Map<String, String> = mapOf(),
        kafkaTemplate: KafkaTemplate<String?, GenericRecord>,
    ): ListenableFuture<SendResult<String?, GenericRecord>> {
        val producerRecord = ProducerRecord(topic, key, record)

        generate(record, headers).forEach { producerRecord.headers().add(it.key(), it.value()) }

        val result = kafkaTemplate.send(producerRecord)

        result.addCallback({
            logger.debug("Event sent; topic=$topic; key=$key; partition=${it?.recordMetadata?.partition()}; offset=${it?.recordMetadata?.partition()}")
        }, {
            logger.error("Event error; topic=$topic; key=$key", it)
        })

        return result
    }
}
