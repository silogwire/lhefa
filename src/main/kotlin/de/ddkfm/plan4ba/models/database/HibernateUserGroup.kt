package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.Model
import de.ddkfm.plan4ba.models.UserGroup
import javax.persistence.*

@Entity
@Table(name = "[UserGroup]",
        indexes = [
            Index(columnList = "uid")
        ])
data class HibernateUserGroup (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,
        @Column
        var uid : String,

        @OneToOne
        var university: HibernateUniversity
) : Model<UserGroup>{
        override fun toModel(): UserGroup {
                return UserGroup(
                    id,
                    uid,
                    university.id
                )
        }
}