package de.ddkfm.stpapp.models

import org.apache.commons.codec.digest.DigestUtils
import javax.persistence.*

data class DatabaseConfig(
        var host : String,
        var port : Int,
        var database : String,
        var username : String,
        var password : String
)
data class Config(
        var database : DatabaseConfig
)
@Entity
data class User (
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Int,

        @Column(nullable = false)
        var username: String,

        @Column(nullable = false)
        var matriculationNumber: String,

        @Column(nullable = true)
        var email: String? = null,

        @Column(nullable = true, length = 32)
        var userHash : String,

        @Column(nullable = false)
        var password : String
) {
        fun withoutPassword() : User = this.copy(password = "");

        fun cleanID() : User = this.copy(id = 0)
        fun generatePasswordHash() : User = this.copy(password = DigestUtils.sha512Hex(this.password))

}

