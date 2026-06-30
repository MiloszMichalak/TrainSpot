package pl.meleko.trainspot.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import pl.meleko.trainspot.requests.UpdateProfileRequest
import pl.meleko.trainspot.service.UserService
import pl.meleko.trainspot.util.jwtUserId
import pl.meleko.trainspot.util.mapToResponse
import pl.meleko.trainspot.util.receiveImageData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Application.installUserRoutes() {
    routing {
        route("/users") {
            get("/check-username") {
                val username = call.request.queryParameters["username"]
                if (username == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                UserService.isUsernameAvailable(username)
                    .mapToResponse()
            }

            authenticate("auth-jwt") {
                // GET /{id} - Get user by ID (public)
                get("/{id}") {
                    val userId = call.parameters["id"]?.let { Uuid.parse(it) }

                    if (userId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }

                    UserService.getProfile(userId)
                        .mapToResponse()
                }

                // GET /me - Get current authenticated user
                get("/me") {
                    val userId = call.jwtUserId()

                    UserService.getProfile(userId)
                        .mapToResponse()
                }

                // PUT /me - Update current user's profile
                put("/me") {
                    val userId = call.jwtUserId()
                    val request = call.receive<UpdateProfileRequest>()
                    val incomingImage = call.receiveImageData()

                    UserService.updateProfile(userId, request, incomingImage)
                        .mapToResponse()
                }

                // DELETE /me - Delete current user's account
                delete("/me") {
                    val userId = call.jwtUserId()

                    UserService.deleteProfile(userId)
                        .mapToResponse()
                }

                // GET /{id} - Get user by ID
                get("/{id}") {
                    val userId = call.parameters["id"]?.let { Uuid.parse(it) }
                    val currentUserId = call.jwtUserId()

                    if (currentUserId != userId) {
                        call.respond(HttpStatusCode.Forbidden)
                        return@get
                    }

                    UserService.getProfile(userId)
                        .mapToResponse()
                }
            }
        }
    }
}
