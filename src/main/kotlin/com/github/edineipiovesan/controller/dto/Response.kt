package com.github.edineipiovesan.controller.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.apache.avro.generic.GenericRecord

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Response(
    val topic: String? = null,
    val partition: String? = null,
    val offset: String? = null,
    val key: String? = null,
    @JsonSerialize(using = AvroJsonSerializer::class)
    val value: GenericRecord? = null,
    val headers: Map<String, String>? = null
)

class AvroJsonSerializer: JsonSerializer<GenericRecord>() {
    override fun serialize(value: GenericRecord, generator: JsonGenerator, provider: SerializerProvider) {
        generator.writeRawValue(value.toString())
    }
}
