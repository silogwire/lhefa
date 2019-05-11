package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.LectureChange
import de.ddkfm.plan4ba.models.Model
import javax.persistence.*

@Entity
@Table(name = "[LectureChange]")
data class HibernateLectureChange(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Int,
    @OneToOne
    var notification : HibernateNotification,

    @OneToOne(optional = true)
    var old : HibernateLecture?,

    @OneToOne(optional = true)
    var new : HibernateLecture?
) : Model<LectureChange> {
    override fun toModel(): LectureChange {
        return LectureChange(
            id,
            notification.id,
            old?.id,
            new?.id
        )
    }
}