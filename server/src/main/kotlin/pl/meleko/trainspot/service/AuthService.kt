package pl.meleko.trainspot.service

import io.ktor.http.HttpStatusCode
import pl.meleko.trainspot.database.model.toUser
import pl.meleko.trainspot.repository.SessionsRepository
import pl.meleko.trainspot.repository.UsersRepository
import pl.meleko.trainspot.requests.LoginRequest
import pl.meleko.trainspot.requests.RegisterRequest
import pl.meleko.trainspot.response.AuthResponse
import pl.meleko.trainspot.util.Bcrypt.checkPassword
import pl.meleko.trainspot.util.Bcrypt.hashPassword
import pl.meleko.trainspot.util.JwtUtil
import pl.meleko.trainspot.util.NetworkResult
import kotlin.uuid.Uuid

object AuthService {
    suspend fun register(request: RegisterRequest): NetworkResult<AuthResponse> {
        val newUserId = UsersRepository.insert(
            email = request.email,
            passwordHash = request.password.hashPassword(),
        ) ?: return NetworkResult.Error(HttpStatusCode.Conflict)

        val user  = UsersRepository.findById(newUserId)?.toUser()
            ?: return NetworkResult.Error(HttpStatusCode.InternalServerError)

        val sessionId = SessionsRepository.create(newUserId)
        val token = JwtUtil.createToken(newUserId, sessionId)
        val refreshToken = JwtUtil.createRefreshToken(newUserId, sessionId)

        return NetworkResult.Success(AuthResponse(token = token, refreshToken = refreshToken, user = user))
    }

    suspend fun login(request: LoginRequest): NetworkResult<AuthResponse> {
        val user = UsersRepository.findByEmailOrUsername(email = request.emailOrUsername, username = request.emailOrUsername)
            ?: return NetworkResult.Error(HttpStatusCode.Unauthorized)

        if (!user.passwordHash.checkPassword(request.password)) {
            return NetworkResult.Error(HttpStatusCode.Unauthorized)
        }

        val sessionId = SessionsRepository.create(user.id)
        val token = JwtUtil.createToken(user.id, sessionId)
        val refreshToken = JwtUtil.createRefreshToken(user.id, sessionId)

        return NetworkResult.Success(AuthResponse(token = token, refreshToken = refreshToken, user = user.toUser()))
    }

    suspend fun logout(sessionId: Uuid): NetworkResult<Unit> {
        val deleted = SessionsRepository.delete(sessionId)

        return if (deleted) NetworkResult.Success(Unit)
        else NetworkResult.Error(HttpStatusCode.InternalServerError)
    }

    suspend fun logoutAll(userId: Uuid): NetworkResult<Unit> {
        SessionsRepository.deleteAllByUserId(userId)
        return NetworkResult.Success(Unit)
    }

    suspend fun checkSession(userId: Uuid): NetworkResult<AuthResponse> {
        val user = UsersRepository.findById(userId)?.toUser()
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)

        return NetworkResult.Success(AuthResponse(token = "", refreshToken = "", user = user))
    }

    suspend fun refresh(refreshToken: String): NetworkResult<AuthResponse> {
        val principal = JwtUtil.verifyToken(refreshToken) ?: return NetworkResult.Error(HttpStatusCode.Unauthorized)
        val isRefresh = principal.payload.getClaim("refresh").asBoolean() ?: false
        if (!isRefresh) return NetworkResult.Error(HttpStatusCode.Unauthorized)

        val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
        val sessionId = Uuid.parse(principal.payload.getClaim("sessionId").asString())

        val user = UsersRepository.findById(userId)?.toUser() ?: return NetworkResult.Error(HttpStatusCode.NotFound)

        val newToken = JwtUtil.createToken(userId, sessionId)
        val newRefreshToken = JwtUtil.createRefreshToken(userId, sessionId)

        return NetworkResult.Success(AuthResponse(token = newToken, refreshToken = newRefreshToken, user = user))
    }
}