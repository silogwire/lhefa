package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.Model
import de.ddkfm.plan4ba.models.Notification
import javax.persistence.*

@Entity
@Table(name = "[Notification]")
data class HibernateNotification(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,

        @Column
        var type : String,

        @OneToOne
        var user : HibernateUser,

        @OneToOne(optional = true)
        var version : HibernateAppVersion?
) : Model<Notification> {
        override fun toModel(): Notification {
                return Notification(
                    id,
                    type,
                    user.id,
                    version?.id
                )
        }

}