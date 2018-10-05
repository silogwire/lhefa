package de.ddkfm.plan4ba.utils

import io.swagger.annotations.ApiImplicitParam

fun mapDataTypes(pair : Pair<ApiImplicitParam, String>) : Any {
    return when(pair.first.dataType.toLowerCase()) {
        "integer" -> pair.second.toInt()
        "long" -> pair.second.toLong()
        "boolean" -> pair.second.toBoolean()
        "double" -> pair.second.toDouble()
        else -> pair.second
    }
}

fun getEnvOrDefault(key : String, default : String) : String {
    return System.getenv(key) ?: default
}

