package pl.meleko.trainspot

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import pl.meleko.trainspot.database.configureDatabase
import pl.meleko.trainspot.plugins.configureAuthentication
import pl.meleko.trainspot.plugins.configureCORS
import pl.meleko.trainspot.plugins.configureDI
import pl.meleko.trainspot.plugins.configureSerialization
import pl.meleko.trainspot.plugins.configureStatusPages
import pl.meleko.trainspot.routes.configureRouting

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureStatusPages()
    configureAuthentication()
    configureDatabase()
    configureCORS()
    configureDI()
    configureRouting()
}