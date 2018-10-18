package de.ddkfm.plan4ba.models

import de.ddkfm.plan4ba.utils.getEnvOrDefault

enum class DatabaseType(val type : String) {
    DRIVER_MYSQL("com.mysql.jdbc.Driver"),
    DRIVER_POSTGRESQL("org.postgresql.Driver");

    fun toJDBCType() : String {
        return type.split(".")[1]
    }
}
data class DatabaseConfig(
        var host : String = "172.17.0.1",
        var port : Int = 5432,
        var database : String = "dbservice",
        var username : String = "dbservice",
        var password : String = "dbservice",
        var type : DatabaseType = DatabaseType.DRIVER_POSTGRESQL
)
data class Config(
        var database : DatabaseConfig = DatabaseConfig()
) {
    fun buildFromEnv() {
        this.database = DatabaseConfig()
        this.database.type =
                when(getEnvOrDefault("DATABASE_DRIVER", "postgresql").toLowerCase()) {
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