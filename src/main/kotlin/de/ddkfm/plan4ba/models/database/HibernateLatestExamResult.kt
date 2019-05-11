package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.LatestExamResult
import de.ddkfm.plan4ba.models.Model
import javax.persistence.*

@Entity
@Table(name = "[LatestExamResult]")
data class HibernateLatestExamResult(
    @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,

    @OneToOne
        var reminder : HibernateReminder,

    @Column
        var grade: Double,

    @Column
        var agrDate : Long,

    @Column
        var status : String,

    @Column
        var title : String,

    @Column
        var shortTitle : String,

    @Column
        var type : String
) : Model<LatestExamResult> {
        override fun toModel(): LatestExamResult {
                return LatestExamResult(
                    id,
                    reminder.id,
                    grade,
                    agrDate,
                    status,
                    title,
                    shortTitle,
                    type
                )
        }
}