package de.ddkfm.plan4ba.models

import de.ddkfm.plan4ba.models.database.*

@FunctionalInterface
interface ModelConverter<T, U> {
    fun convert(model : T) : U
}

object UserConverter : ModelConverter<User, HibernateUser> {

    override fun convert(model: User): HibernateUser {
        return HibernateUser(
            id = model.id,
            password = model.password,
            userHash = model.userHash,
            matriculationNumber = model.matriculationNumber,
            group = HibernateUserGroup(model.groupId, "", HibernateUniversity(0, "", "", "")),
            lastLecturePolling = model.lastLecturePolling,
            lastLectureCall = model.lastLectureCall,
            storeExamsStats = model.storeExamsStats,
            storeReminders = model.storeReminders
        )
    }
}

object AppChangeConverter : ModelConverter<AppChange, HibernateAppChange> {
    override fun convert(model: AppChange): HibernateAppChange {
        return HibernateAppChange(
            model.id,
            HibernateAppVersion(model.appVersion,"", 0),
            model.description,
            model.path
        )
    }
}
object AppVersionConverter : ModelConverter<AppVersion, HibernateAppVersion> {
    override fun convert(model: AppVersion): HibernateAppVersion {
        return HibernateAppVersion(
            model.id,
            model.version,
            model.timestamp
        )
    }
}

object ExamStatConverter : ModelConverter<ExamStat, HibernateExamStat> {
    override fun convert(model: ExamStat): HibernateExamStat {
        return HibernateExamStat(
            model.id,
            user = HibernateUser(
                id = model.userId,
                password = "",
                userHash = "",
                matriculationNumber = "",
                group = HibernateUserGroup(0, "", HibernateUniversity(0, "", "", "")),
                lastLecturePolling = 0,
                lastLectureCall = 0
            ),
            mbooked = model.mbooked,
            booked = model.booked,
            exams = model.exams,
            failure = model.failure,
            modules = model.modules,
            success = model.success,
            creditpoints = model.creditpoints
        )
    }
}

object InfoTextConverter : ModelConverter<Infotext, HibernateInfotext> {
    override fun convert(model: Infotext): HibernateInfotext {
        return HibernateInfotext(
            model.id,
            model.key,
            model.description,
            model.language
        )
    }
}

object LatestExamResultConverter : ModelConverter<LatestExamResult, HibernateLatestExamResult> {
    override fun convert(model: LatestExamResult): HibernateLatestExamResult {
        return HibernateLatestExamResult(
            model.id,
            HibernateReminder(model.reminderId, HibernateUser(
                id = 0,
                password = "",
                userHash = "",
                matriculationNumber = "",
                group = HibernateUserGroup(0, "", HibernateUniversity(0, "", "", "")),
                lastLecturePolling = 0,
                lastLectureCall = 0
            ), 0, 0, 0),
            model.grade,
            model.agrDate,
            model.status,
            model.title,
            model.shortTitle,
            model.type
        )
    }
}
object LectureConverter : ModelConverter<Lecture, HibernateLecture> {
    override fun convert(model: Lecture): HibernateLecture {
        return HibernateLecture(
            id = model.id,
            allDay = model.allDay,
            color = model.color,
            description = model.description,
            start = model.start,
            end = model.end,
            instructor = model.instructor,
            exam = model.exam,
            remarks = model.remarks,
            room = model.room,
            sroom = model.sroom,
            title = model.title,
            user = HibernateUser(
                id = model.userId,
                password = "",
                userHash = "",
                matriculationNumber = "",
                group = HibernateUserGroup(0, "", HibernateUniversity(0, "", "", "")),
                lastLecturePolling = 0,
                lastLectureCall = 0
            )
        )
    }
}

object LectureChangeConverter : ModelConverter<LectureChange, HibernateLectureChange> {
    override fun convert(model: LectureChange): HibernateLectureChange {
        return HibernateLectureChange(
            model.id,
            HibernateNotification(
                id = model.notificationId,
                type = "",
                user = HibernateUser(
                    id = 0,
                    password = "",
                    userHash = "",
                    matriculationNumber = "",
                    group = HibernateUserGroup(0, "", HibernateUniversity(0, "", "", "")),
                    lastLecturePolling = 0,
                    lastLectureCall = 0
                ),
                version = null
            ),
            if (model.old != null)
                HibernateLecture(
                    id = model.old ?: -1,
                    allDay = false,
                    color = "",
                    description = "",
                    start = 0,
                    end = 0,
                    instructor = "",
                    exam = false,
                    remarks = "",
                    room = "",
                    sroom = "",
                    title = "",
                    user = HibernateUser(
                        id = 0,
                        password = "",
                        userHash = "",
                        matriculationNumber = "",
                        group = HibernateUserGroup(0, "", HibernateUniversity(0, "", "", "")),
                        lastLecturePolling = 0,
                        lastLectureCall = 0
                    )
                )
            else null,
            if(model.new != null)
                HibernateLecture(
                    id = model.new ?: -1,
                    allDay = false,
                    color = "",
                    description = "",
                    start = 0,
                    end = 0,
                    instructor = "",
                    exam = false,
                    remarks = "",
                    room = "",
                    sroom = "",
                    title = "",
                    user = HibernateUser(
                        id = 0,
                        password = "",
                        userHash = "",
                        matriculationNumber = "",
                        group = HibernateUserGroup(0, "", HibernateUniversity(0, "", "", "")),
                        lastLecturePolling = 0,
                        lastLectureCall = 0
                    )
                )
            else null

        )
    }
}

object NotificationConverter : ModelConverter<Notification, HibernateNotification> {
    override fun convert(model: Notification): HibernateNotification {
        return HibernateNotification(
            model.id,
            model.type,
            user = HibernateUser(
                id = model.userId,
                password = "",
                userHash = "",
                matriculationNumber = "",
                group = HibernateUserGroup(0, "", HibernateUniversity(0, "", "", "")),
                lastLecturePolling = 0,
                lastLectureCall = 0
            ),
            version = model.versionId?.let { HibernateAppVersion(it, "", 0) }
        )
    }
}

object ReminderConverter : ModelConverter<Reminder, HibernateReminder> {
    override fun convert(model: Reminder): HibernateReminder {
        return HibernateReminder(
            model.id,
            user = HibernateUser(
                id = model.userId,
                password = "",
                userHash = "",
                matriculationNumber = "",
                group = HibernateUserGroup(0, "", HibernateUniversity(0, "", "", "")),
                lastLecturePolling = 0,
                lastLectureCall = 0
            ),
            exams = model.exams,
            electives = model.electives,
            semester = model.semester
        )
    }
}

object UpcomingConverter : ModelConverter<Upcoming, HibernateUpcoming> {
    override fun convert(model: Upcoming): HibernateUpcoming {
        return HibernateUpcoming(
            model.id,
            HibernateReminder(
                model.reminderId,
                HibernateUser(
                    id = 0,
                    password = "",
                    userHash = "",
                    matriculationNumber = "",
                    group = HibernateUserGroup(0, "", HibernateUniversity(0, "", "", "")),
                    lastLecturePolling = 0,
                    lastLectureCall = 0
                ),
                0,
                0,
                0),
            model.begin,
            model.end,
            model.shortTitle,
            model.title,
            model.room,
            model.instructor,
            model.comment
        )
    }
}

object TokenConverter : ModelConverter<Token, HibernateToken> {
    override fun convert(model: Token): HibernateToken {
        return HibernateToken(
            token = model.token,
            user = HibernateUser(
                id = model.userId,
                password = "",
                userHash = "",
                matriculationNumber = "",
                group = HibernateUserGroup(0, "", HibernateUniversity(0, "", "", "")),
                lastLecturePolling = 0,
                lastLectureCall = 0
            ),
            validTo = model.validTo,
            isCalDavToken = model.isCalDavToken,
            isRefreshToken = model.isRefreshToken
        )
    }
}

object UniversityConverter : ModelConverter<University, HibernateUniversity> {
    override fun convert(model: University): HibernateUniversity {
        return HibernateUniversity(
            model.id,
            model.name,
            model.accentColor,
            model.logoUrl
        )
    }
}

object UserGroupConverter : ModelConverter<UserGroup, HibernateUserGroup> {
    override fun convert(model: UserGroup): HibernateUserGroup {
        return HibernateUserGroup(
            id = model.id,
            uid = model.uid,
            university = HibernateUniversity(model.universityId, "", "", "")
        )
    }
}