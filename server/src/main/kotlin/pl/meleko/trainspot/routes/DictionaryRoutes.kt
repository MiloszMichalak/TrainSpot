package pl.meleko.trainspot.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import pl.meleko.trainspot.repository.PkpRepository

fun Application.installDictionaryRoutes() {
    routing {
        route("/dictionary") {
            authenticate("auth-jwt") {

                // GET /stations - Get all stations
                get("/stations") {
                    call.respond(HttpStatusCode.OK, PkpRepository.getAllStations())
                }

                // GET /carriers - Get all carriers
                get("/carriers") {
                    call.respond(HttpStatusCode.OK, PkpRepository.getAllCarriers())
                }

                // GET /categories - Get all commercial categories
                get("/categories") {
                    call.respond(HttpStatusCode.OK, PkpRepository.getAllCommercialCategories())
                }
            }
        }
    }
}
