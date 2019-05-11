package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.hardcoded.UniversityData
import de.ddkfm.plan4ba.models.Model
import de.ddkfm.plan4ba.models.University
import javax.persistence.*

@Entity
@Table(name = "[University]",
        indexes = [
            Index(columnList = "name")
        ])
data class HibernateUniversity(
    @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,
    @Column
        var name : String,

    @Column
        var accentColor : String = UniversityData.getInstance(name).accentColor,

    @Column
        var logoUrl : String = UniversityData.getInstance(name).logo
) : Model<University> {

        override fun toModel(): University {
                return University(
                    id,
                    name,
                    accentColor,
                    logoUrl
                )
        }
}