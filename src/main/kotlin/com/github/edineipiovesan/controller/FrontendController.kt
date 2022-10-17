package com.github.edineipiovesan.controller

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.edineipiovesan.common.ObjectMapper
import com.github.edineipiovesan.controller.dto.KafkaCluster
import com.github.edineipiovesan.controller.dto.KafkaEnvironment
import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class FrontendController {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/clusters")
    fun clusters() = KafkaCluster.values().toSet()

    @GetMapping("/environments")
    fun environments() = KafkaEnvironment.values().toSet()

    @GetMapping("/topics")
    fun topics(): List<String?>? {
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url("http://localhost:8081/subjects")
            .build()

        val response: List<String>? = client.newCall(request).execute().body?.string()
            ?.let { ObjectMapper.instance.readValue(it) }
        return response?.map { it.replaceAfterLast("-", "").removeSuffix("-") }
    }

    @GetMapping("/schemas/{topic}")
    fun schemas(@PathVariable topic: String): List<String>? {
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url("http://localhost:8081/subjects")
            .build()

        val response: List<String>? = client.newCall(request).execute().body?.string()
            ?.let { ObjectMapper.instance.readValue(it) }

        return response?.filter { it.startsWith(topic) }?.map { it.removePrefix("$topic-") }
    }

    @GetMapping("/schemas/{topic}/{schema}/versions")
    fun versions(@PathVariable topic: String, @PathVariable schema: String): String? {
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url("http://localhost:8081/subjects/$topic-$schema/versions")
            .build()

        return client.newCall(request).execute().body?.string()
    }

    @GetMapping("/schemas/{topic}/{schema}/{version}")
    fun avro(@PathVariable topic: String, @PathVariable schema: String, @PathVariable version: Int): String? {
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url("http://localhost:8081/subjects/$topic-$schema/versions/$version")
            .build()

        val readTree = ObjectMapper.instance.readTree(client.newCall(request).execute().body?.string())
        return ObjectMapper.instance.readValue(readTree.get("schema").toString())
    }
}
