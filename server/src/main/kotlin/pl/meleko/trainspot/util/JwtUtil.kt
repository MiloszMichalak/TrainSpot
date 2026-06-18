package pl.meleko.trainspot.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import java.util.Date
import kotlin.uuid.Uuid

object JwtUtil {
    private val secret = System.getenv("JWT_SECRET")
    private val issuer = System.getenv("JWT_ISSUER")
    private val audience = System.getenv("JWT_AUDIENCE")

    fun createToken(userId: Uuid, expiresInMs: Long = 3_600_000): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + expiresInMs))
            .sign(Algorithm.HMAC256(secret))
}

fun ApplicationCall.jwtUserId(): Uuid =
    Uuid.parse(principal<JWTPrincipal>()?.payload?.getClaim("userId").toString())