package pl.meleko.trainspot.service

import io.ktor.http.HttpStatusCode
import pl.meleko.trainspot.database.model.toUser
import pl.meleko.trainspot.model.User
import pl.meleko.trainspot.repository.UsersRepository
import pl.meleko.trainspot.requests.UpdateProfileRequest
import pl.meleko.trainspot.util.ImageStorage
import pl.meleko.trainspot.util.IncomingImage
import pl.meleko.trainspot.util.NetworkResult
import kotlin.uuid.Uuid

object UserService {
    suspend fun getProfile(userId: Uuid): NetworkResult<User> {
        val user = UsersRepository.findById(userId) ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        return NetworkResult.Success(user.toUser())
    }

    suspend fun updateProfile(userIdFromToken: Uuid, request: UpdateProfileRequest, incomingImage: IncomingImage?): NetworkResult<User> {
        request.username?.let { username ->
            val existingUser = UsersRepository.findByEmailOrUsername(username = username)
            if (existingUser != null && existingUser.id != userIdFromToken) {
                return NetworkResult.Error(HttpStatusCode.Conflict)
            }
        }

        val avatarUrl = incomingImage?.let {
            ImageStorage.save(it, "avatars", userIdFromToken)
        }

        val updated = UsersRepository.update(userIdFromToken, request.email, request.username, avatarUrl, request.bio)
        if (!updated) return NetworkResult.Error(HttpStatusCode.InternalServerError)

        val user = UsersRepository.findById(userIdFromToken)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        return NetworkResult.Success(user.toUser())
    }

    suspend fun isUsernameAvailable(username: String): NetworkResult<Boolean> {
        val user = UsersRepository.findByEmailOrUsername(username = username)
        return NetworkResult.Success(user == null)
    }

    suspend fun deleteProfile(userId: Uuid): NetworkResult<Unit> {
        if (UsersRepository.delete(userId)) return NetworkResult.Error(HttpStatusCode.InternalServerError)
        return NetworkResult.Success(Unit)
    }
}