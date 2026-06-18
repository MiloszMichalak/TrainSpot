package pl.meleko.trainspot.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import pl.meleko.trainspot.requests.SpotRequest
import pl.meleko.trainspot.service.SpotsService
import pl.meleko.trainspot.util.jwtUserId
import pl.meleko.trainspot.util.mapToResponse
import pl.meleko.trainspot.util.receiveImageData
import kotlin.uuid.Uuid

fun Application.installSpotRoutes() {
    routing {
        authenticate("auth-jwt") {
            route("/spots") {
                // GET  - Get all spots (paginated) - Used for feed
                get("/") {
                    val page = call.parameters["page"]?.toIntOrNull() ?: 0
                    val limit = call.parameters["limit"]?.toIntOrNull() ?: 20

                    SpotsService.findAll(page, limit)
                        .mapToResponse()
                }

                // POST  - Create a new spot
                post("/") {
                    val userId = call.jwtUserId()
                    val request = call.receive<SpotRequest>()
                    val incomingImage = call.receiveImageData()

                    SpotsService.create(userId, request, incomingImage)
                        .mapToResponse()
                }

                // GET /{id} - Get spot by ID (public)
                get("/{id}") {
                    val spotId = call.parameters["id"]?.let { Uuid.parse(it) }

                    if (spotId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }

                    SpotsService.getById(spotId)
                        .mapToResponse()
                }

                // PUT /{id} - Update a spot
                put("/{id}") {
                    val spotId = call.parameters["id"]?.let { Uuid.parse(it) }
                    val userId = call.jwtUserId()
                    val request = call.receive<SpotRequest>()

                    if (spotId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@put
                    }

                    val imageUrl = call.receiveImage(
                        "spots",
                        spotId
                    )

                    SpotsService.update(spotId, userId, request, imageUrl)
                        .mapToResponse()
                }

                // DELETE /{id} - Delete a spot
                delete("/{id}") {
                    val spotId = call.parameters["id"]?.let { Uuid.parse(it) }
                    val userId = call.jwtUserId()

                    if (spotId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@delete
                    }

                    SpotsService.delete(spotId, userId)
                        .mapToResponse()
                }

                // GET /user/{userId} - Get spots by user ID
                get("/user/{userId}") {
                    val userId = call.parameters["userId"]?.let { Uuid.parse(it) }

                    if (userId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }

                    val page = call.parameters["page"]?.toIntOrNull() ?: 0
                    val limit = call.parameters["limit"]?.toIntOrNull() ?: 20

                    SpotsService.findByUser(userId, page, limit)
                        .mapToResponse()
                }

                // GET /user/me - Get current user's spots
                get("/user/me") {
                    val userId = call.jwtUserId()

                    val page = call.parameters["page"]?.toIntOrNull() ?: 0
                    val limit = call.parameters["limit"]?.toIntOrNull() ?: 20

                    SpotsService.findByUser(userId, page, limit)
                        .mapToResponse()
                }
            }
        }
    }
}
