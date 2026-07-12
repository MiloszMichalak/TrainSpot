package pl.meleko.trainspot.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import pl.meleko.trainspot.service.LikesService
import pl.meleko.trainspot.service.SpotsService
import pl.meleko.trainspot.util.jwtUserId
import pl.meleko.trainspot.util.mapToResponse
import kotlin.uuid.Uuid

fun Routing.installLikeRoutes() {
    route("/likes"){
        authenticate("auth-jwt") {
            // POST /spots/{spotId} - Like a spot
            post("/spots/{spotId}") {
                val spotId = call.parameters["spotId"]?.let { Uuid.parse(it) }

                spotId?.let {
                    LikesService.createLike(userId = call.jwtUserId(), spotId = it).mapToResponse()
                } ?: call.respond(HttpStatusCode.BadRequest)
            }

            // DELETE /spots/{spotId} - Unlike a spot
            delete("/spots/{spotId}") {
                val spotId = call.parameters["spotId"]?.let { Uuid.parse(it) }

                LikesService.removeLike(userId = call.jwtUserId(), spotId = spotId)
                    .mapToResponse()
            }

            // GET /user/{userId} - Get all spots liked by a user
            get("/user/{userId}") {
                val userId = call.parameters["userId"]?.let { Uuid.parse(it) }

                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 20

                SpotsService.getLikedSpots(userId, page, limit)
                    .mapToResponse()
            }

            // GET /user/me - Get current user's liked spots
            get("/user/me") {
                val userId = call.jwtUserId()

                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 20

                SpotsService.getLikedSpots(userId, page, limit)
                    .mapToResponse()
            }
        }
    }
}
