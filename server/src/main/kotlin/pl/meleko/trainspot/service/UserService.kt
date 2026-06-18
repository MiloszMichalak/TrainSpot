package pl.meleko.trainspot.service

import io.ktor.http.HttpStatusCode
import pl.meleko.trainspot.database.model.UserDto
import pl.meleko.trainspot.repository.UsersRepository
import pl.meleko.trainspot.requests.UpdateProfileRequest
import pl.meleko.trainspot.util.NetworkResult
import kotlin.uuid.Uuid

object UserService {
    fun getProfile(userId: Uuid): NetworkResult<UserDto> {
        val user = UsersRepository.findById(userId) ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        return NetworkResult.Success(user)
    }

    fun updateProfile(userIdFromToken: Uuid, request: UpdateProfileRequest, avatarUrl: String?): NetworkResult<UserDto> {
        val updated = UsersRepository.update(userIdFromToken, request.email, request.username, avatarUrl, request.bio)
        if (!updated) return NetworkResult.Error(HttpStatusCode.InternalServerError)

        val user = UsersRepository.findById(userIdFromToken)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        return NetworkResult.Success(user)
    }

    fun updateAvatar(userIdFromToken: Uuid, avatarUrl: String?): NetworkResult<UserDto> {
        UsersRepository.update(
            userIdFromToken,
            avatarUrl = avatarUrl
        )

        val user = UsersRepository.findById(userIdFromToken)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        return NetworkResult.Success(user)
    }
}