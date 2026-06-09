package pl.meleko.trainspot.routes

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import pl.meleko.trainspot.plugins.ApiResponse

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respond(ApiResponse.ok(mapOf("status" to "ok")))
        }

        get("/") {
            val port = environment.config.property("server.port")?.getString() ?: "8080"
            call.respond(ApiResponse.ok(mapOf(
                "message" to "RailSpotter API is running on port $port"
            )))
        }
    }
}
