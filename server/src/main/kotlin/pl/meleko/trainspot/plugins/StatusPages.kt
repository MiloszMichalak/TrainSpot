package pl.meleko.trainspot.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest)
        }

        exception<IllegalStateException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized)
        }

        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound)
        }

        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError)
        }

        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(HttpStatusCode.NotFound)
        }
    }
}

class NotFoundException(message: String) : Exception(message)