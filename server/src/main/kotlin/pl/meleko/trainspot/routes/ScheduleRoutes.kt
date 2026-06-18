package pl.meleko.trainspot.routes

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import pl.meleko.trainspot.service.ScheduleService
import pl.meleko.trainspot.util.mapToResponse

fun Application.installScheduleRoutes() {
    routing {
        authenticate("auth-jwt") {
            route("/schedule"){
                // GET /station/{id}/recent - Get recent trains at a station
                get("/station/{id}/recent") {
                    val stationId = call.parameters["id"]?.toInt()
                    val minutes = call.parameters["minutes"]?.toLongOrNull() ?: 60

                    ScheduleService.getRecentTrains(stationId, minutes).mapToResponse()
                }

                // GET /train/{number}/today - Get today's route for a train
                get("/train/{number}/today") {
                    val stationId = call.parameters["number"]?.toIntOrNull()

                    ScheduleService.getTodayTrainsForStation(stationId).mapToResponse()
                }
            }
        }
    }
}
