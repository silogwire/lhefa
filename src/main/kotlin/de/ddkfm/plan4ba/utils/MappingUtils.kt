package de.ddkfm.plan4ba.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

fun mapDataTypes(pair : Pair<Class<*>, String?>) : Any? {
    val returnValue = when(pair.first) {
        Int::class.java -> pair.second?.toIntOrNull() ?: -1
        Long::class.java -> pair.second?.toLongOrNull() ?: -1
        Boolean::class.java -> pair.second?.toBoolean() ?: false
        Double::class.java -> pair.second?.toDoubleOrNull() ?: -1
        else -> pair.second
    } ?: ""
    return returnValue
}

fun getEnvOrDefault(key : String, default : String) : String {
    return System.getenv(key) ?: default
}


fun Any.toJson() : String {
    return jacksonObjectMapper().writeValueAsString(this)
}
inline fun <reified T> String.toModel() : T? {
    return  jacksonObjectMapper().readValue(this, T::class.java)
}
inline fun <reified T> String.toListModel() : List<T>? {
    return  jacksonObjectMapper().readValue(this, jacksonObjectMapper().typeFactory.constructCollectionType(List::class.java, T::class.java))
}

fun LocalDateTime.toMillis() : Long {
    return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
fun LocalDate.toMillis() : Long {
    return LocalDateTime.of(this.year, this.month, this.dayOfMonth, 12, 0).toMillis()
}
