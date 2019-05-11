package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.Infotext
import de.ddkfm.plan4ba.models.Model
import org.hibernate.annotations.Type
import javax.persistence.*

@Entity
@Table(name = "[Infotext]",
        indexes = [
            Index(columnList = "key")])
data class HibernateInfotext(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Int,
        @Column
        var key : String,
        @Lob
        @Type(type = "text")
        @Column(length = Integer.MAX_VALUE)
        var description: String,
        @Column(length = 5)
        var language : String = "de"

) : Model<Infotext> {
        override fun toModel(): Infotext {
                return Infotext(
                    id,
                    key,
                    description,
                    language
                )
        }
}