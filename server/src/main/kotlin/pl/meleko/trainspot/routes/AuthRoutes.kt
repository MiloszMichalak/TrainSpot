package pl.meleko.trainspot.routes

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import pl.meleko.trainspot.requests.LoginRequest
import pl.meleko.trainspot.requests.RegisterRequest
import pl.meleko.trainspot.service.AuthService
import pl.meleko.trainspot.util.jwtSessionId
import pl.meleko.trainspot.util.jwtUserId
import pl.meleko.trainspot.util.mapToResponse
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun Application.installAuthRoutes() {
    routing {
        route("/auth"){
            post("/register") {
                val request = call.receive<RegisterRequest>()

                AuthService.register(request)
                    .mapToResponse()
            }

            post("/login") {
                val request = call.receive<LoginRequest>()

                AuthService.login(request)
                    .mapToResponse()
            }

            authenticate("auth-jwt") {
                post("/logout") {
                    val sessionId = call.jwtSessionId()

                    AuthService.logout(sessionId)
                        .mapToResponse()
                }

                // POST /logout-all
                post("/logout-all") {
                    val userId = call.jwtUserId()

                    AuthService.logoutAll(userId)
                        .mapToResponse()
                }

                // POST /refresh
                post("/refresh") {
                    val userId = call.jwtUserId()
                    val sessionId = call.jwtSessionId()

                    AuthService.refreshToken(userId, sessionId)
                        .mapToResponse()
                }
            }
        }
    }
}