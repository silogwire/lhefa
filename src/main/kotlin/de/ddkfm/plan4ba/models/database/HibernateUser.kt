package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.Model
import de.ddkfm.plan4ba.models.User
import org.apache.commons.codec.digest.DigestUtils
import org.hibernate.annotations.ColumnDefault
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
        var lastLectureCall : Long,

    @Column(nullable = false)
    @ColumnDefault("false")
    var storeExamsStats : Boolean = false,

    @Column(nullable = false)
    @ColumnDefault("false")
    var storeReminders : Boolean = false
) : Model<User> {
        fun withoutPassword() : HibernateUser = this.copy(password = "")

        fun cleanID() : HibernateUser = this.copy(id = 0)
        fun generatePasswordHash() : HibernateUser = this.copy(password = DigestUtils.sha512Hex(this.password))

        override fun toModel(): User {
                return User(
                    id,
                    matriculationNumber,
                    userHash,
                    password,
                    group.id,
                    lastLecturePolling,
                    lastLectureCall,
                    storeExamsStats,
                    storeReminders
                )
        }
}