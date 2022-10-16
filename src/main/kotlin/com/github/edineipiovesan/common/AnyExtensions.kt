package com.github.edineipiovesan.common

fun Any?.convertToString(): String? = when (this) {
    is ByteArray -> decodeToString()
    else -> this?.toString()
}
