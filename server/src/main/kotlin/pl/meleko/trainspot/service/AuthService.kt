package pl.meleko.trainspot.service

import io.ktor.http.HttpStatusCode
import org.mindrot.jbcrypt.BCrypt
import pl.meleko.trainspot.AuthResponse
import pl.meleko.trainspot.database.model.UserDto
import pl.meleko.trainspot.database.model.toUser
import pl.meleko.trainspot.repository.SessionsRepository
import pl.meleko.trainspot.repository.UsersRepository
import pl.meleko.trainspot.requests.LoginRequest
import pl.meleko.trainspot.requests.RegisterRequest
import pl.meleko.trainspot.util.JwtUtil
import pl.meleko.trainspot.util.NetworkResult
import kotlin.uuid.Uuid

fun String.hashPassword(): String =
    BCrypt.hashpw(this, BCrypt.gensalt())

fun String.checkPassword(plain: String): Boolean =
    BCrypt.checkpw(plain, this)

object AuthService {

    fun register(request: RegisterRequest): NetworkResult<AuthResponse> {
        val newUserId = UsersRepository.insert(
            email = request.email,
            passwordHash = request.password.hashPassword(),
            username = request.username
        ) ?: return NetworkResult.Error(HttpStatusCode.Conflict)

        val user  = UsersRepository.findById(newUserId)?.toUser()
            ?: return NetworkResult.Error(HttpStatusCode.InternalServerError)
        SessionsRepository.create(newUserId)
        val token = JwtUtil.createToken(newUserId)

        return NetworkResult.Success(AuthResponse(token = token, user = user))
    }

    fun login(request: LoginRequest): NetworkResult<AuthResponse> {
        val user = UsersRepository.findByEmailOrUsername(email = request.emailOrUsername, username = request.emailOrUsername)
            ?: return NetworkResult.Error(HttpStatusCode.Unauthorized)

        if (!user.passwordHash.checkPassword(request.password)) {
            return NetworkResult.Error(HttpStatusCode.Unauthorized)
        }

        SessionsRepository.create(user.id)
        val token = JwtUtil.createToken(user.id)

        return NetworkResult.Success(AuthResponse(token = token, user = user.toUser()))
    }

    fun logout(sessionId: Uuid): NetworkResult<Unit> {
        val deleted = SessionsRepository.delete(sessionId)
        return if (deleted) NetworkResult.Success(Unit)
        else NetworkResult.Error(HttpStatusCode.InternalServerError)
    }

    fun logoutAll(userId: Uuid): NetworkResult<Unit> {
        SessionsRepository.deleteAllByUserId(userId)
        return NetworkResult.Success(Unit)
    }

    fun refreshToken(userId: Uuid): NetworkResult<String> {
        val newToken = JwtUtil.createToken(userId)
        return NetworkResult.Success(newToken)
    }

    fun getProfile(userId: Uuid): NetworkResult<UserDto> {
        val user = UsersRepository.findById(userId)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        return NetworkResult.Success(user)
    }

    fun updateProfile(
        userId: Uuid,
        email: String?,
        username: String?,
        avatarUrl: String?,
        bio: String?
    ): NetworkResult<UserDto> {
        val updated = UsersRepository.update(userId, email, username, avatarUrl, bio)
        if (!updated) return NetworkResult.Error(HttpStatusCode.InternalServerError)

        val user = UsersRepository.findById(userId)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        return NetworkResult.Success(user)
    }
}