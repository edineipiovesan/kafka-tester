package com.github.edineipiovesan.controller.dto

import com.github.edineipiovesan.controller.dto.KafkaEnvironment.DEV
import com.fasterxml.jackson.databind.JsonNode

data class Request(
    val topic: String,
    val headers: Map<String, String>,
    val key: String? = null,
    val avro: JsonNode,
    val schemaFullName: String? = null,
    val schema: String = "",
    val kafkaCluster: KafkaCluster = KafkaCluster.LOCAL,
    val kafkaEnvironment: KafkaEnvironment = DEV,
    val producerProperties: Map<String, String> = mapOf()
)

enum class KafkaCluster {
    SHARD, LOCAL
}

enum class KafkaEnvironment {
    DEV, HOM
}
