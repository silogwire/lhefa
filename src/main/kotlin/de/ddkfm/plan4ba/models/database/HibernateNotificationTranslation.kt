package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.Model
import de.ddkfm.plan4ba.models.NotificationTranslation
import javax.persistence.*


@Entity
@Table(name = "[NotificationTranslation]")
data class HibernateNotificationTranslation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Int,
    @Column
    var type : String,
    @Column
    var language : String,
    @Column
    var label : String,
    @Column
    var description : String
) : Model<NotificationTranslation> {
    override fun toModel(): NotificationTranslation {
        return NotificationTranslation(
            id,
            type,
            language,
            label,
            description
        )
    }
}