package de.ddkfm.plan4ba.models

import de.ddkfm.plan4ba.hardcoded.UniversityData
import org.apache.commons.codec.digest.DigestUtils
import org.hibernate.annotations.Type
import javax.persistence.*

@Entity
@Table(name = "[User]",
        indexes = [Index(columnList = "matriculationNumber")]
)
data class HibernateUser (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int,

        @Column(nullable = false)
        var matriculationNumber: String,

        @Column(nullable = true, length = 32)
        var userHash : String?,

        @Column(nullable = false, length = 128)
        var password : String,

        @OneToOne
        var group : HibernateUserGroup,

        @Column
        var lastLecturePolling : Long,

        @Column
        var lastLectureCall : Long
) {
        fun withoutPassword() : HibernateUser = this.copy(password = "")

        fun cleanID() : HibernateUser = this.copy(id = 0)
        fun generatePasswordHash() : HibernateUser = this.copy(password = DigestUtils.sha512Hex(this.password))
        fun toUser() = User(
                id = this.id,
                matriculationNumber = this.matriculationNumber,
                userHash = this.userHash,
                password = this.password,
                groupId = this.group.id,
                lastLectureCall = this.lastLectureCall,
                lastLecturePolling = this.lastLecturePolling
        )
}
@Entity
@Table(name = "[Token]",
        indexes = [
            Index(columnList = "token"),
            Index(columnList = "user_id")
        ])
data class HibernateToken(
        @Id
        @Column
        var token : String,

        @OneToOne
        var user : HibernateUser,

        @Column
        var isCalDavToken : Boolean,

        @Column
        var isRefreshToken : Boolean,

        @Column
        var validTo : Long
) {
        fun toToken() = Token(
                token = this.token,
                validTo = this.validTo,
                userId = this.user.id,
                isCalDavToken = this.isCalDavToken,
                isRefreshToken = this.isRefreshToken
        )
}

@Entity
@Table(name = "[UserGroup]",
        indexes = [
            Index(columnList = "uid")
        ])
data class HibernateUserGroup(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,
        @Column
        var uid : String,

        @OneToOne
        var university: HibernateUniversity
) {
        fun toUserGroup() = UserGroup(
                id = this.id,
                uid = this.uid,
                universityId = this.university.id
        )
}

@Entity
@Table(name = "[University]",
        indexes = [
            Index(columnList = "name")
        ])
data class HibernateUniversity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,
        @Column
        var name : String,

        @Column
        var accentColor : String = UniversityData.getInstance(name).accentColor,

        @Column
        var logoUrl : String = UniversityData.getInstance(name).logo
) {

        fun toUniversity() = University(
                id = this.id,
                name = this.name,
                accentColor = this.accentColor,
                logoUrl = this.logoUrl
        )
}

@Entity
@Table(name = "[Infotext]",
        indexes = [
        Index(columnList = "key")])
data class HibernateInfotext(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,
        @Column
        var key : String,
        @Lob
        @Type(type = "text")
        @Column(length = Integer.MAX_VALUE)
        var description: String,
        @Column(length = 5)
        var language : String = "de"

) {
        fun toInfotext() : Infotext {
                return Infotext(
                        id = this.id,
                        key = this.key,
                        description = this.description,
                        language = this.language
                )
        }
}
@Entity
@Table(name = "[Lecture]",
        indexes = [
            Index(columnList = "user_id")
        ])
data class HibernateLecture(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,
        @Column
        var title : String,
        @Column(name = "[start]")
        var start : Long,
        @Column(name = "[end]")
        var end : Long,
        @Column
        var allDay : Boolean,
        @Column
        var description : String,
        @Column
        var color : String,
        @Column
        var room : String,
        @Column
        var sroom : String,
        @Column
        var instructor : String,
        @Column
        var remarks : String,
        @Column
        var exam : Boolean,
        @OneToOne
        var user : HibernateUser
) {
        fun toLecture() = Lecture(
                id = this.id,
                userId = this.user.id,
                description = this.description,
                start = this.start,
                allDay = this.allDay,
                color = this.color,
                end = this.end,
                instructor = this.instructor,
                exam = this.exam,
                remarks = this.remarks,
                room = this.room,
                sroom = this.sroom,
                title = this.title
        )
}

@Entity
@Table(name = "[Notification]")
data class HibernateNotification(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,
        @Column
        var label : String,
        @Column
        var description : String,

        @Column
        var type : String,
        /*
        @Column
        var viewed : Boolean,
        @Column
        var data : String?,
        */
        @OneToOne
        var user : HibernateUser
) {
        fun toNotification() : Notification {
                return Notification(
                        id = this.id,
                        label = this.label,
                        description =  this.description,
                        type = this.type,
                        /*viewed = this.viewed,
                        data = null,*/
                        userId = this.user.id
                )
        }
}

@Entity
@Table(name = "[Link]")
data class HibernateLink(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,
        @Column
        var label : String,
        @Column
        var url : String,
        @OneToOne
        var university : HibernateUniversity?,
        @OneToOne
        var group : HibernateUserGroup?,
        @Column(length = 5)
        var language : String = "de"
) {
        fun toLink() : Link {
                return Link(
                        id = this.id,
                        label = this.label,
                        url = this.url,
                        universityId = this.university?.id ?: -1,
                        groupId = this.group?.id ?: -1,
                        language = this.language
                )
        }
}

@Entity
@Table(name = "[ExamStats]")
data class HibernateExamStats(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,

        @OneToOne
        var user : HibernateUser,

        @Column
        var booked : Int,
        @Column
        var exams : Int,
        @Column
        var failure : Int,
        @Column
        var mbooked : Int,
        @Column
        var modules : Int,
        @Column
        var success : Int
) {
    fun toExamStats() : ExamStats {
        return ExamStats(
                id = this.id,
                userId = this.user.id,
                booked = this.booked,
                exams = this.exams,
                failure = this.failure,
                mbooked = this.mbooked,
                modules = this.modules,
                success = this.success
        )
    }
}


@Entity
@Table(name = "[Reminder]")
data class HibernateReminder(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,

        @OneToOne
        var user : HibernateUser,

        @Column
        var semester : Int,
        @Column
        var exams: Int,
        @Column
        var electives : Int
)

@Entity
@Table(name = "[LatestExamResult]")
data class HibernateLatestExamResult(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,

        @OneToOne
        var reminder : HibernateReminder,

        @Column
        var grade: Double,

        @Column
        var status : String,

        @Column
        var title : String,

        @Column
        var shortTitle : String,

        @Column
        var type : String
)