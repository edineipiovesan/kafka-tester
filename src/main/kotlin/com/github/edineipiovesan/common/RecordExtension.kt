package com.github.edineipiovesan.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.github.edineipiovesan.common.Kafka.BOOTSTRAP_SERVERS
import com.github.edineipiovesan.common.ObjectMapper.instance
import com.github.edineipiovesan.controller.dto.Request
import org.apache.avro.Schema.Parser
import org.apache.avro.generic.GenericRecord
import org.springframework.kafka.core.KafkaTemplate
import tech.allegro.schema.json2avro.converter.JsonAvroConverter
import java.lang.Class.forName

fun generateRecord(request: Request): GenericRecord {
    return generateRecordBySchema(request.avro.toString(), request.schema.toString())
}

fun generateRecordBySchema(record: String, schema: String): GenericRecord {
    val converter = JsonAvroConverter()
    val parsedSchema = Parser().parse(schema)
    val recordToByteArray = record.toByteArray()

    return converter.convertToGenericDataRecord(recordToByteArray, parsedSchema)
}

fun stringToSpecificRecord(record: String, schemaFullName: String): GenericRecord {
    val avroSchemaClass = forName(schemaFullName)

    return instance.readValue(record, avroSchemaClass) as GenericRecord
}

fun requestNewTransaction(request: Request): Request {
    val dataNode = (request.avro.path("data") as ObjectNode)
    val newData: JsonNode = dataNode.set("id", TextNode(randomUUIDString()))
    val newAvro: JsonNode = (request.avro as ObjectNode).set("data", newData)

    return request.copy(avro = newAvro)
}

fun KafkaTemplate<String?, GenericRecord>.bootstrapServers() =
    producerFactory.configurationProperties[BOOTSTRAP_SERVERS]
