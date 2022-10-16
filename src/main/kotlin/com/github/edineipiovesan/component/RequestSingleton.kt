package com.github.edineipiovesan.component

import com.github.edineipiovesan.common.KafkaHeaderBuilder
import com.github.edineipiovesan.controller.dto.Request
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.header.Headers
import org.springframework.kafka.core.KafkaTemplate

object RequestSingleton {
    var producer: Producer? = null

    fun off() {
        producer?.let { producer = Producer(producer!!.copy(isOn = false)) }
    }

    fun on(
        request: Request,
        kafkaTemplate: KafkaTemplate<String?, GenericRecord>,
        record: GenericRecord
    ) {
        producer = Producer(
            isOn = true,
            request = request,
            record = record,
            headers = KafkaHeaderBuilder.generate(record, request.headers),
            kafkaTemplate = kafkaTemplate
        )
    }

    fun isOn(): Boolean = producer?.isOn ?: false
}

data class Producer(
    var isOn: Boolean,
    val request: Request,
    val record: GenericRecord,
    val headers: Headers,
    val kafkaTemplate: KafkaTemplate<String?, GenericRecord>,
) {
    constructor(producer: Producer) : this(
        isOn = producer.isOn,
        request = producer.request,
        record = producer.record,
        headers = producer.headers,
        kafkaTemplate = producer.kafkaTemplate
    )
}
