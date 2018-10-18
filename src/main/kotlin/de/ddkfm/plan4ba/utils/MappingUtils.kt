package de.ddkfm.plan4ba.utils

import de.ddkfm.plan4ba.jacksonObjectMapper
import de.ddkfm.plan4ba.models.*
import io.swagger.annotations.ApiImplicitParam
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun mapDataTypes(pair : Pair<ApiImplicitParam, String>) : Any {
    val returnValue = when(pair.first.dataType.toLowerCase()) {
        "integer" -> pair.second.toIntOrNull()
        "long" -> pair.second.toLongOrNull()
        "boolean" -> pair.second.toBoolean()
        "double" -> pair.second.toDoubleOrNull()
        else -> pair.second
    }
    return returnValue ?: getDefaultValue(pair.first.dataType.toLowerCase())
}

fun getDefaultValue(type : String) : Any {
    return when(type) {
        "integer", "long", "double" -> -1
        else -> ""
    }
}

fun getEnvOrDefault(key : String, default : String) : String {
    return System.getenv(key) ?: default
}

fun User.toHibernateUser() : HibernateUser {
    return HibernateUser(
            id = this.id,
            password = this.password,
            userHash = this.userHash,
            matriculationNumber = this.matriculationNumber,
            group = HibernateUserGroup(this.groupId, "", HibernateUniversity(0, "", "", "")),
            lastLecturePolling = this.lastLecturePolling,
            lastLectureCall = this.lastLectureCall
    )
}

fun UserGroup.toHibernateUserGroup() : HibernateUserGroup {
    return HibernateUserGroup(
            id = this.id,
            uid = this.uid,
            university = HibernateUniversity(this.universityId, "", "", "")
    )
}

fun Lecture.toHibernateLecture() : HibernateLecture {
    return HibernateLecture(
            id = this.id,
            allDay = this.allDay,
            color = this.color,
            description = this.description,
            start = this.start,
            end = this.end,
            instructor = this.instructor,
            exam = this.exam,
            remarks = this.remarks,
            room = this.room,
            sroom = this.sroom,
            title = this.title,
            user = HibernateUser(
                    id = this.userId,
                    password = "",
                    userHash = "",
                    matriculationNumber = "",
                    group = HibernateUserGroup(0, "", HibernateUniversity(0, "", "", "")),
                    lastLecturePolling = 0,
                    lastLectureCall = 0
            )
    )
}

fun Token.toHibernateToken() : HibernateToken {
    return HibernateToken(
            token = this.token,
            user = HibernateUser(
                    id = this.userId,
                    password = "",
                    userHash = "",
                    matriculationNumber = "",
                    group = HibernateUserGroup(0, "", HibernateUniversity(0, "", "", "")),
                    lastLecturePolling = 0,
                    lastLectureCall = 0
            ),
            validTo = this.validTo,
            isCalDavToken = this.isCalDavToken
    )
}

fun Any.toJson() : String {
    return jacksonObjectMapper().writeValueAsString(this)
}
fun <T> JSONObject.toModel(type : Class<T>) : T {
    return jacksonObjectMapper().readValue(this.toString(), type)
}

fun LocalDateTime.toMillis() : Long {
    return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
fun LocalDate.toMillis() : Long {
    return LocalDateTime.of(this.year, this.month, this.dayOfMonth, 12, 0).toMillis()
}
