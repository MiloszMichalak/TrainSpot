package pl.meleko.trainspot.util

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.config.ApplicationConfig
import java.util.Date
import kotlin.uuid.Uuid

object JwtUtil {
    private lateinit var secret: String
    private lateinit var issuer: String
    private lateinit var audience: String

    fun configure(config: ApplicationConfig) {
        secret = config.property("jwt.secret").getString()
        issuer = config.property("jwt.issuer").getString()
        audience = config.property("jwt.audience").getString()
    }

    fun createToken(userId: Uuid, sessionId: Uuid, expiresInMs: Long = 3_600_000): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId.toString())
            .withClaim("sessionId", sessionId.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + expiresInMs))
            .sign(Algorithm.HMAC256(secret))
    }

    fun createRefreshToken(userId: Uuid, sessionId: Uuid, expiresInMs: Long = 2_592_000_000): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId.toString())
            .withClaim("sessionId", sessionId.toString())
            .withClaim("refresh", true)
            .withExpiresAt(Date(System.currentTimeMillis() + expiresInMs))
            .sign(Algorithm.HMAC256(secret))
    }

    fun verifyToken(token: String): JWTPrincipal? {
        return try {
            val decodedJWT = getVerifier().verify(token)
            JWTPrincipal(decodedJWT)
        } catch (e: Exception) {
            null
        }
    }

    fun getVerifier(): JWTVerifier{
        return JWT.require(Algorithm.HMAC256(secret))
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
    }
}

fun ApplicationCall.jwtUserId(): Uuid =
    Uuid.parse(principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()!!)

fun ApplicationCall.jwtSessionId(): Uuid =
    Uuid.parse(principal<JWTPrincipal>()?.payload?.getClaim("sessionId")?.asString()!!)
