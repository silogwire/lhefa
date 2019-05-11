package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.ExamStat
import de.ddkfm.plan4ba.models.Model
import javax.persistence.*

@Entity
@Table(name = "[ExamStat]")
data class HibernateExamStat(
    @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,

    @OneToOne
        var user : HibernateUser,

    @Column
        var booked : Int,
    @Column
        var exams : Int,
    @Column
        var failure : Int,
    @Column
        var mbooked : Int,
    @Column
        var modules : Int,
    @Column
        var success : Int,
    @Column
    var creditpoints : Int
) : Model<ExamStat> {
    override fun toModel(): ExamStat {
        return ExamStat(
            id,
            user.id,
            booked,
            exams,
            failure,
            mbooked,
            modules,
            success,
            creditpoints
        )
    }

}