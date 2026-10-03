package pl.meleko.trainspot.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import pl.meleko.trainspot.requests.SpotRequest
import pl.meleko.trainspot.service.SpotsService
import pl.meleko.trainspot.util.jwtUserId
import pl.meleko.trainspot.util.mapToResponse
import pl.meleko.trainspot.util.receiveImageData
import kotlin.uuid.Uuid

fun Routing.installSpotRoutes() {
    authenticate("auth-jwt") {
        route("/spot") {
            // GET  - Get all spots (paginated) - Used for feed
            get {
                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 20

                SpotsService.findAll(page, limit, call.jwtUserId())
                    .mapToResponse()
            }

            // POST  - Create a new spot
            post {
                val userId = call.jwtUserId()
                val request = call.receive<SpotRequest>()

                SpotsService.create(userId, request)
                    .mapToResponse()
            }

            // GET /{id} - Get spot by ID (public)
            get("/{id}") {
                val spotId = call.parameters["id"]?.let { Uuid.parse(it) }

                if (spotId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                SpotsService.getById(spotId, call.jwtUserId())
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

                SpotsService.update(spotId, userId, request)
                    .mapToResponse()
            }

            // PUT /{id}/image - Upload or replace the spot image
            put("/{id}/image") {
                val spotId = call.parameters["id"]?.let { Uuid.parse(it) }
                val userId = call.jwtUserId()

                if (spotId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@put
                }

                SpotsService.uploadImage(spotId, userId, call.receiveImageData())
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

                SpotsService.findByUser(userId, page, limit, call.jwtUserId())
                    .mapToResponse()
            }

            // GET /user/me - Get current user's spots
            get("/user/me") {
                val userId = call.jwtUserId()

                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 20

                SpotsService.findByUser(userId, page, limit, userId)
                    .mapToResponse()
            }
        }
    }
}
