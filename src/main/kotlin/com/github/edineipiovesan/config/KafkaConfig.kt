package com.github.edineipiovesan.config

import org.apache.avro.generic.GenericRecord
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate

@EnableKafka
@Configuration
class KafkaConfig(
    properties: KafkaProperties,
    private val environmentProperties: EnvironmentProperties
) {
    private val shard = (properties.producer + environmentProperties.shard.kafka.properties)
    private val local = (properties.producer + environmentProperties.local.kafka.properties)

    fun kafkaTemplateLocal(producerProperties: Map<String, String>): KafkaTemplate<String?, GenericRecord> =
        KafkaTemplate(
            DefaultKafkaProducerFactory(local + environmentProperties.local.kafka.bootstrapLocal + producerProperties)
        )

    fun kafkaTemplateShard(producerProperties: Map<String, String>): KafkaTemplate<String?, GenericRecord> =
        KafkaTemplate(
            DefaultKafkaProducerFactory(shard + environmentProperties.shard.kafka.bootstrapShard + producerProperties)
        )
}
