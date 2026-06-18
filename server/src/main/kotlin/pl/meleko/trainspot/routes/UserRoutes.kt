package pl.meleko.trainspot.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.staticFiles
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import pl.meleko.trainspot.requests.UpdateProfileRequest
import pl.meleko.trainspot.service.UserService
import pl.meleko.trainspot.util.jwtUserId
import pl.meleko.trainspot.util.mapToResponse
import pl.meleko.trainspot.util.receiveImage
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Application.installUserRoutes() {
    routing {
        staticFiles("/avatars", File("/var/www/trainspot/avatars"))

        authenticate("auth-jwt") {
            // GET /users/{id} - Get user by ID (public)
            get("/users/{id}") {
                val userId = call.parameters["id"]?.let { Uuid.parse(it) }

                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                UserService.getProfile(userId)
                    .mapToResponse()
            }

            // GET /users/me - Get current authenticated user
            get("/users/me") {
                val userId = call.jwtUserId()

                UserService.getProfile(userId)
                    .mapToResponse()
            }

            // PUT /users/me - Update current user's profile
            put("/users/me") {
                val userId = call.jwtUserId()
                val request = call.receive<UpdateProfileRequest>()

                val avatarUrl = call.receiveImage(
                    subdir = "avatar",
                    id = userId,
                )

                UserService.updateProfile(userId, request, avatarUrl)
                    .mapToResponse()
            }

            // DELETE /users/me - Delete current user's account
            delete("/users/me") {
                val userId = call.jwtUserId()

                // TODO: Implement account deletion in UserService
                call.respond(HttpStatusCode.NotImplemented)
            }

            // GET /users/{id} - Get user by ID (authenticated)
            get("/users/{id}") {
                val userId = call.parameters["id"]?.let { Uuid.parse(it) }
                val currentUserId = call.jwtUserId()

                // Only allow users to view their own profile without authentication
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
