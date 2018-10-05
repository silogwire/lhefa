package de.ddkfm.plan4ba.models

import com.fasterxml.jackson.annotation.JsonFormat
import de.ddkfm.plan4ba.utils.getEnvOrDefault
import org.apache.commons.codec.digest.DigestUtils
import java.util.*
import javax.persistence.*

enum class DatabaseType(val type : String) {
        DRIVER_MYSQL("com.mysql.jdbc.Driver"),
        DRIVER_POSTGRESQL("org.postgresql.Driver"),
}
data class DatabaseConfig(
        var host : String = "localhost",
        var port : Int = 3306,
        var database : String = "dbservice",
        var username : String = "dbservice",
        var password : String = "dbservice",
        var type : DatabaseType = DatabaseType.DRIVER_MYSQL
)
data class Config(
        var database : DatabaseConfig = DatabaseConfig()
) {
        fun buildFromEnv() {
                this.database = DatabaseConfig()
                this.database.type =
                        when(getEnvOrDefault("DATABASE_DRIVER", "MySQL").toLowerCase()) {
                                "postgresql" -> DatabaseType.DRIVER_POSTGRESQL
                                "mysql" -> DatabaseType.DRIVER_MYSQL
                                else -> DatabaseType.DRIVER_MYSQL
                        }
                this.database.host = getEnvOrDefault("DATABASE_HOST", this.database.host)
                this.database.port = getEnvOrDefault("DATABASE_PORT", this.database.port.toString()).toInt()
                this.database.database = getEnvOrDefault("DATABASE_NAME", this.database.database)
                this.database.username = getEnvOrDefault("DATABASE_USER", this.database.username)
                this.database.password = getEnvOrDefault("DATABASE_PASSWORD", this.database.password)
        }
}

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
        var password : String,

        @OneToOne
        var group : UserGroup?,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss'.'S'Z'")
        var lastLogin : Date?
) {
        fun withoutPassword() : User = this.copy(password = "");

        fun cleanID() : User = this.copy(id = 0)
        fun generatePasswordHash() : User = this.copy(password = DigestUtils.sha512Hex(this.password))

}
@Entity
data class UserGroup(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id : Int,
        @Column
        var uid : String,
        @Column
        var name : String
)

@Entity
data class Lecture(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id : Int,
        @Column
        var title : String,
        @Column
        var start : Long,
        @Column
        var end : Long,
        @Column
        var allDay : Boolean,
        @Column
        var description : String,
        @Column
        var color : String,
        @Column
        var room : String,
        @Column
        var sroom : String,
        @Column
        var instructor : String,
        @Column
        var remarks : String
)

