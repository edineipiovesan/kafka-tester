package com.github.edineipiovesan.common

import org.apache.avro.generic.GenericRecord

/**
 * Transforma o fullName do schema para snakecase.
 */
fun <T : GenericRecord> T.getSchemaQualifiedName() =
    schema.fullName.replace("([A-Z])".toRegex(), "_$1").lowercase().replaceFirst("_", "")

/**
 * Transforma o event name do schema para snakecase.
 */
fun <T : GenericRecord> T.getSchemaName() =
    schema.name.replace("([A-Z])".toRegex(), "_$1").removePrefix("_").lowercase()