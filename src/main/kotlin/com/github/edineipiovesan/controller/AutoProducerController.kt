package com.github.edineipiovesan.controller

import com.github.edineipiovesan.common.KafkaHeaderBuilder
import com.github.edineipiovesan.common.bootstrapServers
import com.github.edineipiovesan.common.generateRecord
import com.github.edineipiovesan.common.requestNewTransaction
import com.github.edineipiovesan.component.KafkaProducer
import com.github.edineipiovesan.component.KafkaProducerResolver
import com.github.edineipiovesan.controller.dto.Request
import com.github.edineipiovesan.controller.dto.Response
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.header.Headers
import org.slf4j.LoggerFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auto-producer")
class AutoProducerController(
    private val kafkaProducerResolver: KafkaProducerResolver,
    private val applicationContext: ConfigurableApplicationContext
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private var kafkaProducer: KafkaProducer? = null
    lateinit var eventGenerator: () -> Pair<String?, GenericRecord>
    lateinit var headerGenerator: (record: GenericRecord) -> Headers
    lateinit var kafkaTemplate: KafkaTemplate<String?, GenericRecord>
    lateinit var topicName: String

    @PostMapping("/on")
    private fun enable() {
        logger.info("Creating ${KafkaProducer::class.simpleName} bean...")
        val beanFactory = applicationContext.beanFactory
        kafkaProducer = beanFactory.createBean(KafkaProducer::class.java)

        logger.info("Updating ${KafkaProducer::class.simpleName} parameters...")
        kafkaProducer?.eventGenerator = eventGenerator
        kafkaProducer?.headerGenerator = headerGenerator
        kafkaProducer?.kafkaTemplate = kafkaTemplate
        kafkaProducer?.topicName = topicName

        logger.info("All parameters updated.")
    }

    @PostMapping("/off")
    private fun disabled() {
        val beanFactory = applicationContext.beanFactory
        beanFactory.destroyBean(kafkaProducer!!)
    }

    @PostMapping("/definitions")
    private fun setDefinitions(@RequestBody request: Request): Response {
        val environment = request.kafkaEnvironment
        val cluster = request.kafkaCluster
        val topic = request.topic
        val headers = request.headers
        val key = request.key

        kafkaTemplate = kafkaProducerResolver.resolve(request)
        topicName = topic
        headerGenerator = { KafkaHeaderBuilder.generate(it,  headers) }
        eventGenerator = {
            val transaction = requestNewTransaction(request)
            key to generateRecord(transaction)
        }

        logger.info("All ${KafkaProducer::class.simpleName} parameters have been set. " +
            "kafkaCluster=$cluster; " +
            "kafkaEnvironment=$environment; " +
            "address=${kafkaTemplate.bootstrapServers()} " +
            "topic=$topic; " +
            "headers=$headers; " +
            "key=$key")

        enable()

        return Response(topic = topic, key = key, value = null, headers = headers)
    }
}
