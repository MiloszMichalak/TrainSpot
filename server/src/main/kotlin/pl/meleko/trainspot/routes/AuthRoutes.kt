package pl.meleko.trainspot.routes

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import io.ktor.utils.io.readBuffer
import kotlinx.io.readByteArray
import pl.meleko.trainspot.requests.LoginRequest
import pl.meleko.trainspot.requests.RegisterRequest
import pl.meleko.trainspot.requests.UpdateProfileRequest
import pl.meleko.trainspot.service.AuthService
import pl.meleko.trainspot.util.onError
import pl.meleko.trainspot.util.onSuccess
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Application.installAuthRoutes() {
    routing {
//        staticFiles("/avatars", File("/var/www/trainspot/avatars"))

        post("/auth/register") {
            val request = call.receive<RegisterRequest>()

            AuthService.register(request)
                .onSuccess { response -> call.respond(HttpStatusCode.Created, response) }
                .onError { status -> call.respond(status) }
        }

        post("/auth/login") {
            val request = call.receive<LoginRequest>()

            AuthService.login(request)
                .onSuccess { response -> call.respond(HttpStatusCode.OK, response) }
                .onError { status -> call.respond(status) }
        }

        authenticate("auth-jwt") {
            post("/auth/logout") {
                val userId = call.jwtUserId()

                AuthService.logout(userId)
                    .onSuccess { call.respond(HttpStatusCode.OK) }
                    .onError { status -> call.respond(status) }
            }

            // POST /auth/logout-all
            post("/auth/logout-all") {
                val userId = call.jwtUserId()
                
                AuthService.logoutAll(userId)
                    .onSuccess { call.respond(HttpStatusCode.OK) }
                    .onError   { status-> call.respond(status) }
            }

            // POST /auth/refresh
            post("/auth/refresh") {
                val userId = call.jwtUserId()
                
                AuthService.refreshToken(userId)
                    .onSuccess { newToken -> call.respond(HttpStatusCode.OK, mapOf("token" to newToken)) }
                    .onError   { status -> call.respond(status) }
            }

            // GET /auth/me
            get("/auth/me") {
                val userId = call.jwtUserId()

                AuthService.getProfile(userId)
                    .onSuccess { user -> call.respond(HttpStatusCode.OK, user) }
                    .onError   { status -> call.respond(status) }
            }

            // PUT /auth/me
            put("/auth/me") {
                val userId = call.jwtUserId()
                val request = call.receive<UpdateProfileRequest>()

                val uuid = Uuid.generateV4()
                var url = "https://trainspot.meleko.pl/avatars/$userId/$uuid."

                when (val multipart = call.receiveMultipart().readPart()) {
                    is PartData.FileItem -> {
                        val imageExtension = multipart.originalFileName?.substringAfterLast(".") ?: "jpg"
                        val bytes = multipart.provider().readBuffer().readByteArray()
                        val path = "/var/trainspot/avatars/${userId}/$uuid.$imageExtension"
                        File(path).writeBytes(bytes)
                        url += imageExtension
                    }
                    else -> Unit
                }

                AuthService.updateProfile(
                    userId = userId,
                    email = request.email,
                    username = request.username,
                    avatarUrl = url,
                    bio = request.bio
                )
                    .onSuccess { user -> call.respond(HttpStatusCode.OK, user) }
                    .onError   { status -> call.respond(status) }
            }
        }
    }
}

private fun ApplicationCall.jwtUserId(): Uuid =
    Uuid.parse(principal<JWTPrincipal>()?.payload?.getClaim("userId").toString())