package pl.meleko.trainspot.routes

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        get("/") {
            val port = environment.config.property("server.port").getString() ?: "8080"
            call.respond(mapOf(
                "message" to "Trainspot API is running on port $port"
            ))
        }
    }
}
