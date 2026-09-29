package pl.meleko.trainspot.routes

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import pl.meleko.trainspot.service.ScheduleService
import pl.meleko.trainspot.util.mapToResponse

fun Routing.installScheduleRoutes() {
    authenticate("auth-jwt") {
        route("/schedule"){
            // GET /station/{id}/recent - Get recent trains at a station
            get("/station/{id}/recent") {
                val stationId = call.parameters["id"]?.toInt()
                val minutes = call.request.queryParameters["minutes"]?.toLongOrNull() ?: 60

                ScheduleService.getRecentTrains(stationId, minutes).mapToResponse()
            }

            // GET /station/{id}/today - Get today's trains at a station
            get("/station/{id}/today") {
                val stationId = call.parameters["id"]?.toIntOrNull()

                ScheduleService.getTodayTrainsForStation(stationId).mapToResponse()
            }
        }
    }
}
