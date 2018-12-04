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
        var description: String

) {
        fun toInfotext() : Infotext {
                return Infotext(
                        id = this.id,
                        key = this.key,
                        description = this.description
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


