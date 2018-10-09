package de.ddkfm.plan4ba.models

import io.swagger.annotations.ApiModelProperty
import org.apache.commons.codec.digest.DigestUtils
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "[User]")
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
        var storeHash : Boolean,

        @Column
        var hasUserSpecificCalendar : Boolean,

        @Column(nullable = true)
        var lastLogin : LocalDateTime?
) {
        fun withoutPassword() : HibernateUser = this.copy(password = "")

        fun cleanID() : HibernateUser = this.copy(id = 0)
        fun generatePasswordHash() : HibernateUser = this.copy(password = DigestUtils.sha512Hex(this.password))

}
@Entity
@Table(name = "[UserGroup]")
data class HibernateUserGroup(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,
        @Column
        var uid : String,

        @OneToOne
        var university: HibernateUniversity
)
@Entity
@Table(name = "[University]")
data class HibernateUniversity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,
        @Column
        var name : String
)

@Entity
@Table(name = "[Lecture]")
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
        var user : HibernateUser?,
        @OneToOne
        var group : HibernateUserGroup?
)


