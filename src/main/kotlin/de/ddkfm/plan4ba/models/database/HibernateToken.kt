package de.ddkfm.plan4ba.models.database

import de.ddkfm.plan4ba.models.Model
import de.ddkfm.plan4ba.models.Token
import javax.persistence.*

@Entity
@Table(name = "[Token]",
        indexes = [
            Index(columnList = "token"),
            Index(columnList = "user_id")
        ])
data class HibernateToken(
    @Id
        @Column
        var token : String,

    @OneToOne
        var user : HibernateUser,

    @Column
        var isCalDavToken : Boolean,

    @Column
        var isRefreshToken : Boolean,

    @Column
        var validTo : Long
) : Model<Token> {
        override fun toModel(): Token {
                return Token(
                    token,
                    user.id,
                    isCalDavToken,
                    validTo,
                    isRefreshToken
                )
        }

}