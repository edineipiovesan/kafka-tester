package com.github.edineipiovesan.common

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeaders
import java.time.OffsetDateTime.now
import java.time.ZoneId.of
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import com.github.edineipiovesan.common.Timezone.OFFSET_ZONE

object KafkaHeaderBuilder {

    fun generate(record: GenericRecord, originalHeaders: Map<String, Any?>): Headers {
        val recordHeaders: Headers = RecordHeaders()

        originalHeaders
            .forEach { recordHeaders.add(it.key,  it.value.convertToString()?.toByteArray()) }

        KafkaHeaderEnum.values()
            .filterNot { originalHeaders.keys.contains(it.key) }
            .forEach { recordHeaders.add(it.key, it.value()) }

        recordHeaders.lastHeader("type") ?: recordHeaders.add("type", record.getSchemaQualifiedName().toByteArray())

        return recordHeaders
    }

    internal enum class KafkaHeaderEnum(val key: String, val value: () -> ByteArray) {
        TRANSACTION_ID("transactionid", { randomUUIDString().toByteArray() }),
        CORRELATION_ID("correlationid", { randomUUIDString().toByteArray() }),
        TIME("time", { now(of(OFFSET_ZONE)).format(ISO_OFFSET_DATE_TIME).toByteArray() }),
        ID("id", { randomUUIDString().toByteArray() }),
        EVENT_VERSION("eventversion", { "1.0".toByteArray() }),
        SPEC_VERSION("specversion", { "1.0".toByteArray() }),
        DATA_CONTENT_TYPE("datacontenttype", { "application/avro".toByteArray() }),
        MESSAGE_VERSION( "messageversion", { "1.0".toByteArray() });

        companion object {
            operator fun contains(key: String) = values().any { it.key == key }
        }
    }
}
