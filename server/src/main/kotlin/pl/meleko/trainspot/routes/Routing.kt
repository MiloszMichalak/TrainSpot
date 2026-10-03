package pl.meleko.trainspot.routes

import io.ktor.server.application.Application
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.io.File

fun Application.configureRouting() {
    routing {
        staticFiles("/avatars", File("/var/www/trainspot/avatars"))
        staticFiles("/spots", File("/var/www/trainspot/spots"))

        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        get("/") {
            val port = environment.config.property("server.port").getString() ?: "8080"
            call.respond(
                mapOf(
                    "message" to "Trainspot API is running on port $port"
                )
            )
        }

        installAuthRoutes()
        installSpotRoutes()
        installScheduleRoutes()
        installDictionaryRoutes()
        installLikeRoutes()
        installCommentRoutes()
        installUserRoutes()
    }
}
