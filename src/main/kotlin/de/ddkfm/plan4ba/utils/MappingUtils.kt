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
            groupId = this.group.id,
            hasUserSpecificCalendar = this.hasUserSpecificCalendar,
            storeUserHash = this.storeHash
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

fun HibernateLecture.toLecture() : Lecture {
    return Lecture(
            id = this.id,
            title = this.title,
            sroom = this.sroom,
            room = this.room,
            remarks = this.remarks,
            exam = this.exam,
            instructor = this.instructor,
            end = this.end,
            description = this.description,
            color = this.color,
            allDay = this.allDay,
            groupId = this.group?.id ?: 0,
            userId = this.user?.id ?: 0,
            start = this.start
    )
}

fun User.toHibernateUser() : HibernateUser {
    return HibernateUser(
            id = this.id,
            password = this.password,
            userHash = this.userHash,
            lastLogin = this.lastLogin?.toLocalDateTime(),
            matriculationNumber = this.matriculationNumber,
            group = HibernateUserGroup(this.groupId, "", HibernateUniversity(0, "")),
            hasUserSpecificCalendar = this.hasUserSpecificCalendar,
            storeHash = this.storeUserHash
    )
}

fun UserGroup.toHibernateUserGroup() : HibernateUserGroup {
    return HibernateUserGroup(
            id = this.id,
            uid = this.uid,
            university = HibernateUniversity(
                    id = 0,
                    name = ""
            )
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
            user = if(this.userId != null )
                    HibernateUser(this.userId!!, "", "", "", HibernateUserGroup(0, "", HibernateUniversity(0, "")),
                            storeHash = false, hasUserSpecificCalendar = false, lastLogin = 0L.toLocalDateTime())
                else
                    null,
            group = if(this.groupId != null)
                        HibernateUserGroup(this.groupId!!, "", HibernateUniversity(0, ""))
                    else
                        null

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
