package com.github.edineipiovesan.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object ObjectMapper {
    val instance: ObjectMapper = jacksonObjectMapper()
}
