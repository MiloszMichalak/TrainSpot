package pl.meleko.trainspot.di

import io.ktor.server.application.Application
import org.koin.dsl.module
import pl.meleko.trainspot.database.DatabaseConfig
import pl.meleko.trainspot.database.DatabaseFactory
import javax.sql.DataSource

val appModule = module {
    single {
        val app = get<Application>()
        val config = app.environment.config

        DatabaseConfig(
            url = config.property("database.url").getString(),
            user = config.property("database.user").getString(),
            password = config.property("database.password").getString(),
            driver = config.property("database.driver").getString(),
            maxPoolSize = config.property("database.maxPoolSize").getString().toIntOrNull() ?: 20,
            minIdle = config.property("database.minIdle").getString().toIntOrNull() ?: 5,
        )
    }

    single<DataSource> {
        DatabaseFactory.create(get())
    }
}