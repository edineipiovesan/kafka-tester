package com.github.edineipiovesan.controller

import com.github.edineipiovesan.common.bootstrapServers
import com.github.edineipiovesan.common.generateRecord
import com.github.edineipiovesan.common.randomUUIDString
import com.github.edineipiovesan.common.requestNewTransaction
import com.github.edineipiovesan.component.GenericKafkaProducer
import com.github.edineipiovesan.component.KafkaProducerResolver
import com.github.edineipiovesan.component.KafkaProducerSingleton
import com.github.edineipiovesan.component.RequestSingleton
import com.github.edineipiovesan.controller.dto.Request
import com.github.edineipiovesan.controller.dto.Response
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class KafkaController(
    private val genericKafkaProducer: GenericKafkaProducer,
    private val kafkaProducerResolver: KafkaProducerResolver,
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/")
    private fun sendEvents(@RequestBody request: Request): Response {
        val topic = request.topic
        val headers = request.headers

        val kafkaTemplate = kafkaProducerResolver.resolve(request)
        val record = generateRecord(request)

        genericKafkaProducer.send(
            topic = topic,
            key = request.key,
            record = record,
            headers = headers,
            kafkaTemplate = kafkaTemplate
        )

        logger.info("Event sent to kafkaCluster=${kafkaTemplate.bootstrapServers()}")

        return Response(topic = topic, key = request.key, value = record, headers = headers)
    }

    @PostMapping("/batch/{volume}/interval/{interval}")
    private fun sendEvents(
        @RequestBody request: Request,
        @PathVariable("volume") volume: Long,
        @PathVariable("interval") interval: Long,
    ): Response {
        val topic = request.topic
        val headers = request.headers
        val kafkaTemplate = kafkaProducerResolver.resolve(request)

        (1..volume).forEach { _ ->
            val transaction = requestNewTransaction(request)
            val record = generateRecord(transaction)

            Thread.sleep(interval)

            genericKafkaProducer.send(
                topic = topic,
                key = randomUUIDString(),
                record = record,
                headers = headers,
                kafkaTemplate = kafkaTemplate
            )
        }

        logger.info("Events sent to kafkaCluster=${kafkaTemplate.bootstrapServers()} with " +
                "volume=$volume and interval=$interval")

        return Response(topic = request.topic, headers = request.headers)
    }

    @PostMapping("/batch/{volume}/interval/{interval}/keepKeyFor/{keepKeyFor}")
    private fun sendEvents(
        @RequestBody request: Request,
        @PathVariable("volume") volume: Long,
        @PathVariable("interval") interval: Long,
        @PathVariable("keepKeyFor") keepKeyFor: Int,
    ): Response {
        var keyNumberOfTimes = 0
        val topic = request.topic
        val headers = request.headers
        val kafkaTemplate = kafkaProducerResolver.resolve(request)

        val newKey = when {
            ++keyNumberOfTimes <= keepKeyFor -> request.key
            else -> randomUUIDString()
        }

        (1..volume).forEach { _ ->
            val transaction = requestNewTransaction(request)
            val record = generateRecord(transaction)

            Thread.sleep(interval)

            genericKafkaProducer.send(
                topic = topic,
                key = newKey,
                record = record,
                headers = headers,
                kafkaTemplate = kafkaTemplate
            )
        }

        logger.info("Events sent to kafkaCluster=${kafkaTemplate.bootstrapServers()} with volume=$volume, " +
                "interval=$interval, key=$newKey and keepKeyFor=$keepKeyFor")

        return Response(topic = topic, headers = headers)
    }

    @PostMapping("/tps/off")
    private fun offSendByTPS(@RequestBody request: Request): Response {
        val topic = request.topic
        val headers = request.headers
        val record = generateRecord(request)

        RequestSingleton.off()

        return Response(topic = topic, key = request.key, value = record, headers = headers)
    }

    @PostMapping("/tps/{tps}")
    private fun sendByTPS(@RequestBody request: Request, @PathVariable("tps") tps: Long): Response {
        val topic = request.topic
        val headers = request.headers
        val kafkaTemplate = kafkaProducerResolver.resolve(request)
        val record = generateRecord(request)

        RequestSingleton.off()

        val kafkaProducerSingleton = KafkaProducerSingleton()

        RequestSingleton.on(
            request = request,
            record = record,
            kafkaTemplate = kafkaTemplate,
        )

        runBlocking { kafkaProducerSingleton.scheduleProducer(tps) }

        logger.info("Event sent to kafkaCluster=${kafkaTemplate.bootstrapServers()}")

        return Response(topic = topic, key = request.key, value = record, headers = headers)
    }
}
