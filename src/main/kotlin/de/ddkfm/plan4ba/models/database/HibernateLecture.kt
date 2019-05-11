package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.Lecture
import de.ddkfm.plan4ba.models.Model
import org.hibernate.annotations.ColumnDefault
import javax.persistence.*

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
        var user : HibernateUser,
    @Column(nullable = false)
        @ColumnDefault("false")
        var deprecated : Boolean = false
) : Model<Lecture> {
        override fun toModel(): Lecture {
                return Lecture(
                    id,
                    title,
                    start,
                    end,
                    allDay,
                    description,
                    color,
                    room,
                    sroom,
                    instructor,
                    remarks,
                    exam,
                    user.id,
                    deprecated
                )
        }
}