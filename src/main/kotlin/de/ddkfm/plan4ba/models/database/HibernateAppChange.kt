package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.AppChange
import de.ddkfm.plan4ba.models.Model
import javax.persistence.*

@Entity
@Table(name = "[AppChange]")
data class HibernateAppChange(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Int,
    @OneToOne
    var appVersion : HibernateAppVersion,
    @Column
    var description: String,
    @Column
    var path : String
) : Model<AppChange> {
    override fun toModel(): AppChange {
        return AppChange(
            id,
            appVersion.id,
            description,
            path
        )
    }

}