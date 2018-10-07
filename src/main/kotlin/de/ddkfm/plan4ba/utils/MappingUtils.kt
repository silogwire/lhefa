package de.ddkfm.plan4ba.utils

import de.ddkfm.plan4ba.jacksonObjectMapper
import de.ddkfm.plan4ba.models.*
import io.swagger.annotations.ApiImplicitParam
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

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


fun HibernateUser.toUser() : User {
    return User(
            id = this.id,
            matriculationNumber = this.matriculationNumber,
            lastLogin = this.lastLogin?.toMillis() ?: 0,
            userHash = this.userHash,
            password = this.password,
            groupId = this.group.id
    )
}

fun HibernateUserGroup.toUserGroup() : UserGroup {
    return UserGroup(
            id = this.id,
            uid = this.uid,
            universityId = this.university.id
    )
}

fun HibernateUniversity.toUniversity() : University {
    return University(
            id = this.id,
            name = this.name
    )
}

fun User.toHibernateUser() : HibernateUser {
    return HibernateUser(
            id = 0,
            password = this.password,
            userHash = this.userHash,
            lastLogin = this.lastLogin?.toLocalDateTime(),
            matriculationNumber = this.matriculationNumber,
            group = HibernateUserGroup(this.groupId, "", HibernateUniversity(0, ""))
    )
}

fun UserGroup.toHibernateUserGroup() : HibernateUserGroup {
    return HibernateUserGroup(
            id = 0,
            uid = this.uid,
            university = HibernateUniversity(
                    id = 0,
                    name = ""
            )
    )
}


fun Long.toLocalDateTime() : LocalDateTime {
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this),
            TimeZone.getDefault().toZoneId())
    return dateTime
}

fun LocalDateTime.toMillis() : Long {
    return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun Any.toJson() : String {
    return jacksonObjectMapper().writeValueAsString(this)
}
fun <T> JSONObject.toModel(type : Class<T>) : T {
    return jacksonObjectMapper().readValue(this.toString(), type)
}
