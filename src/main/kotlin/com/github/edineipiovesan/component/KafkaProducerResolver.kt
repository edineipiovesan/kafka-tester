package com.github.edineipiovesan.component

import com.github.edineipiovesan.config.KafkaConfig
import com.github.edineipiovesan.controller.dto.KafkaCluster.LOCAL
import com.github.edineipiovesan.controller.dto.KafkaCluster.SHARD
import com.github.edineipiovesan.controller.dto.Request
import org.apache.avro.generic.GenericRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaProducerResolver(private val kafkaConfig: KafkaConfig) {

    fun resolve(request: Request): KafkaTemplate<String?, GenericRecord> {
        return when (request.kafkaCluster) {
            SHARD -> kafkaConfig.kafkaTemplateShard(request.producerProperties)
            LOCAL ->kafkaConfig.kafkaTemplateLocal(request.producerProperties)
        }
    }
}

