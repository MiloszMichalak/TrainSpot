package pl.meleko.trainspot.database

import io.ktor.server.application.Application
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val driver: String = "org.postgresql.Driver"
)

fun Application.configureDatabase() {
    val config = environment.config
    val databaseConfig = DatabaseConfig(
        url = config.property("database.url").getString(),
        user = config.property("database.user").getString(),
        password = config.property("database.password").getString(),
    )
    
    Database.connect(
        url = databaseConfig.url,
        user = databaseConfig.user,
        driver = databaseConfig.driver,
        password = databaseConfig.password,
    )

    transaction {
        SchemaUtils.create(
            UsersTable,
            SessionsTable,
            TrainTypesTable,
            TrainVehiclesTable,
            TrainModelsTable,
            SpotsTable,
            LikesTable,
            CommentsTable,
            CommentLikesTable,
            ScheduleTable,
            TrainStopsTable,
            CarriersTable,
            CommercialCategoriesTable,
            StationsTable
        )
    }
}
