package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.Model
import de.ddkfm.plan4ba.models.Notification
import org.hibernate.annotations.ColumnDefault
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
        var version : HibernateAppVersion?,

        @Column(nullable = false)
        @ColumnDefault("1558370324000")//Workaround, dass in alten Notifications nicht 0 drinnen steht
        var timestamp : Long
) : Model<Notification> {
        override fun toModel(): Notification {
                return Notification(
                    id,
                    type,
                    user.id,
                    version?.id,
                    timestamp
                )
        }

}