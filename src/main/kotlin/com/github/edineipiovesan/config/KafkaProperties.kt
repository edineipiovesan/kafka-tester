package com.github.edineipiovesan.config

import com.github.edineipiovesan.config.KafkaProperties.Companion.PREFIX_PROPERTIES
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = PREFIX_PROPERTIES)
data class KafkaProperties(
    val consumer: Map<String, String>,
    val producer: Map<String, String>
) {

    companion object {
        const val PREFIX_PROPERTIES = "kafka-common"
    }
}
