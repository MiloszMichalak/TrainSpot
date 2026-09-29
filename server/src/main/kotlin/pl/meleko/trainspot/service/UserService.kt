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

    suspend fun updateProfile(userIdFromToken: Uuid, request: UpdateProfileRequest): NetworkResult<User> {
        request.username?.let { username ->
            val normalizedUsername = username.trim()
            if (normalizedUsername.isEmpty() || normalizedUsername.length > 100) {
                return NetworkResult.Error(HttpStatusCode.BadRequest)
            }
            val existingUser = UsersRepository.findByEmailOrUsername(username = normalizedUsername)
            if (existingUser != null && existingUser.id != userIdFromToken) {
                return NetworkResult.Error(HttpStatusCode.Conflict)
            }
        }

        val updated = UsersRepository.update(userIdFromToken, email = request.email, username = request.username?.trim(), bio = request.bio)
        if (!updated) return NetworkResult.Error(HttpStatusCode.InternalServerError)

        val user = UsersRepository.findById(userIdFromToken)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        return NetworkResult.Success(user.toUser())
    }

    suspend fun updateAvatar(userIdFromToken: Uuid, incomingImage: IncomingImage?): NetworkResult<User>{
        if (incomingImage == null || incomingImage.bytes.isEmpty() || incomingImage.bytes.size > 5 * 1024 * 1024) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }
        val extension = incomingImage.originalFileName?.substringAfterLast('.', "")?.lowercase()
        if (extension !in setOf("jpg", "jpeg", "png", "webp")) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }
        if (!incomingImage.hasValidImageSignature(extension.orEmpty())) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }
        val avatarUrl = ImageStorage.save(incomingImage, "avatars", userIdFromToken)

        val updated = UsersRepository.update(id = userIdFromToken, avatarUrl = avatarUrl)
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

    private fun IncomingImage.hasValidImageSignature(extension: String): Boolean = when (extension) {
        "jpg", "jpeg" -> bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()
        "png" -> bytes.size >= 8 && bytes.take(8).map { it.toInt() and 0xFF } == listOf(137, 80, 78, 71, 13, 10, 26, 10)
        "webp" -> bytes.size >= 12 &&
            bytes.copyOfRange(0, 4).decodeToString() == "RIFF" &&
            bytes.copyOfRange(8, 12).decodeToString() == "WEBP"
        else -> false
    }
}
