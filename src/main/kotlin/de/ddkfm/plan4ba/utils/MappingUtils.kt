package de.ddkfm.plan4ba.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.ddkfm.plan4ba.models.*
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
            isCalDavToken = this.isCalDavToken,
            isRefreshToken = this.isRefreshToken
    )
}

fun ExamStats.toHibernateExamStat() : HibernateExamStats {
  return HibernateExamStats(
      id = this.id,
      success = this.success,
      modules = this.modules,
      mbooked = this.mbooked,
      failure = this.failure,
      exams = this.exams,
      booked = this.booked,
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
