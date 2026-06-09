package pl.meleko.trainspot.database

import io.ktor.server.application.Application
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.ktor.ext.inject

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val driver: String = "org.postgresql.Driver"
)

fun Application.configureDatabase() {
    val config by inject<DatabaseConfig>()
    
    Database.connect(
        url = "jdbc:postgresql://${config.user}/${config.url}",
        driver = config.driver,
        user = config.user,
        password = config.password,
    )
}
