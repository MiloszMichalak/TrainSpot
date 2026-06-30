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

        return NetworkResult.Success(AuthResponse(token = token, user = user))
    }

    suspend fun login(request: LoginRequest): NetworkResult<AuthResponse> {
        val user = UsersRepository.findByEmailOrUsername(email = request.emailOrUsername, username = request.emailOrUsername)
            ?: return NetworkResult.Error(HttpStatusCode.Unauthorized)

        if (!user.passwordHash.checkPassword(request.password)) {
            return NetworkResult.Error(HttpStatusCode.Unauthorized)
        }

        val sessionId = SessionsRepository.create(user.id)
        val token = JwtUtil.createToken(user.id, sessionId)

        return NetworkResult.Success(AuthResponse(token = token, user = user.toUser()))
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

    fun refreshToken(userId: Uuid, sessionId: Uuid): NetworkResult<String> {
        val newToken = JwtUtil.createToken(userId, sessionId)
        return NetworkResult.Success(newToken)
    }
}