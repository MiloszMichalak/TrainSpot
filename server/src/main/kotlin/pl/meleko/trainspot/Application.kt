package pl.meleko.trainspot

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pl.meleko.trainspot.database.configureDatabase
import pl.meleko.trainspot.plugins.configureAuthentication
import pl.meleko.trainspot.plugins.configureCORS
import pl.meleko.trainspot.plugins.configureDI
import pl.meleko.trainspot.plugins.configureSerialization
import pl.meleko.trainspot.plugins.configureStatusPages
import pl.meleko.trainspot.remote.PkpDataSeeder
import pl.meleko.trainspot.routes.configureRouting
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

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

    GlobalScope.launch(Dispatchers.IO) {
        while (isActive) {
            val zoneId = ZoneId.of("Europe/Warsaw")
            val now = LocalDateTime.now(zoneId)

            var nextRun = now.withHour(2).withMinute(0).withSecond(0)

            if (now.isAfter(nextRun) || now.isEqual(nextRun)) {
                nextRun = nextRun.plusDays(1)
            }

            val delayMillis = Duration.between(now, nextRun).toMillis()
            delay(delayMillis.milliseconds)

            try {
                PkpDataSeeder.seedAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}