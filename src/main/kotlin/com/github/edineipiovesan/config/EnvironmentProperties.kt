package com.github.edineipiovesan.config

import com.github.edineipiovesan.common.Kafka.BOOTSTRAP_SERVERS
import com.github.edineipiovesan.config.EnvironmentProperties.Companion.PREFIX_PROPERTIES
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = PREFIX_PROPERTIES)
data class EnvironmentProperties(
    val local: Environment,
    val shard: Environment,
) {

    companion object {
        const val PREFIX_PROPERTIES = "environments"
    }
}

data class Environment(
    val kafka: Kafka,
)

data class Kafka(
    val bootstraps: Map<String, String>,
    val properties: Map<String, String>,
) {
    val bootstrapLocal = (BOOTSTRAP_SERVERS to bootstraps["local"])
    val bootstrapShard = (BOOTSTRAP_SERVERS to bootstraps["shard"])
}
