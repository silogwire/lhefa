package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.AppVersion
import de.ddkfm.plan4ba.models.Model
import javax.persistence.*

@Entity
@Table(name = "[AppVersion]")
data class HibernateAppVersion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Int,
    @Column
    var version : String,
    @Column
    var timestamp : Long

) : Model<AppVersion> {
    override fun toModel(): AppVersion {
        return AppVersion(
            id,
            version,
            timestamp
        )
    }
}