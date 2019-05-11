package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.Model
import de.ddkfm.plan4ba.models.Reminder
import javax.persistence.*

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
) : Model<Reminder> {
        override fun toModel(): Reminder {
                return Reminder(
                    id,
                    user.id,
                    semester,
                    exams,
                    electives
                )
        }

}