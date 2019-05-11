package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.Model
import de.ddkfm.plan4ba.models.Upcoming
import javax.persistence.*

@Entity
@Table(name = "[Upcoming]")
data class HibernateUpcoming(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Int,

    @OneToOne
    var reminder : HibernateReminder,

    @Column
    var begin : Long,

    @Column(name = "[end]")
    var end : Long,

    @Column
    var shortTitle : String,

    @Column
    var title : String,

    @Column
    var room : String,

    @Column
    var instructor : String,

    @Column
    var comment : String

) : Model<Upcoming> {
    override fun toModel(): Upcoming {
        return Upcoming(
            id,
            reminder.id,
            begin,
            end,
            shortTitle,
            title,
            room,
            instructor,
            comment
        )
    }

}