package pl.meleko.trainspot.routes

import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import pl.meleko.trainspot.requests.LoginRequest
import pl.meleko.trainspot.requests.RefreshTokenRequest
import pl.meleko.trainspot.requests.RegisterRequest
import pl.meleko.trainspot.service.AuthService
import pl.meleko.trainspot.util.jwtSessionId
import pl.meleko.trainspot.util.jwtUserId
import pl.meleko.trainspot.util.mapToResponse
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun Routing.installAuthRoutes() {
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

        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()

            AuthService.refresh(request.refreshToken)
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

            get("/check-session") {
                val userId = call.jwtUserId()

                AuthService.checkSession(userId)
                    .mapToResponse()
            }
        }
    }
}