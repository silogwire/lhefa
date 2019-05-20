package de.ddkfm.plan4ba.models

interface Model<T> {
    fun toModel() : T
}

fun <U : Model<*>> Any.toHibernate() : U {
    val converter = when(this) {
        is User -> UserConverter
        is AppVersion -> AppVersionConverter
        is ExamStat -> ExamStatConverter
        is LatestExamResult -> LatestExamResultConverter
        is Lecture -> LectureConverter
        is LectureChange -> LectureChangeConverter
        is Notification -> NotificationConverter
        is Reminder -> ReminderConverter
        is Token -> TokenConverter
        is University -> UniversityConverter
        is Upcoming -> UpcomingConverter
        is UserGroup -> UserGroupConverter
        else -> object : ModelConverter<Any, U> {
            override fun convert(model: Any): U {
                return null!!
            }

        }
    } as ModelConverter<Any, U>
    return converter.convert(this)
}