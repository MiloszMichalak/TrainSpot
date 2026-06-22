package pl.meleko.trainspot

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import pl.meleko.trainspot.database.configureDatabase
import pl.meleko.trainspot.jobs.configureScheduler
import pl.meleko.trainspot.plugins.SessionTracker
import pl.meleko.trainspot.plugins.configureAuthentication
import pl.meleko.trainspot.plugins.configureCORS
import pl.meleko.trainspot.plugins.configureDI
import pl.meleko.trainspot.plugins.configureSerialization
import pl.meleko.trainspot.plugins.configureStatusPages
import pl.meleko.trainspot.routes.configureRouting

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureDI()
    configureSerialization()
    configureStatusPages()
    configureAuthentication()
    install(SessionTracker)
    configureDatabase()
    configureCORS()
    configureRouting()
    configureScheduler()
}