package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.Link
import de.ddkfm.plan4ba.models.Model
import javax.persistence.*

@Entity
@Table(name = "[Link]")
data class HibernateLink(
    @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,
    @Column
        var label : String,
    @Column
        var url : String,
    @OneToOne
        var university : HibernateUniversity?,
    @OneToOne
        var group : HibernateUserGroup?,
    @Column(length = 5)
        var language : String = "de"
) : Model<Link> {
        override fun toModel(): Link {
                return Link(
                    id,
                    label,
                    url,
                    university?.id ?: -1,
                    group?.id ?: -1,
                    language
                )
        }
}