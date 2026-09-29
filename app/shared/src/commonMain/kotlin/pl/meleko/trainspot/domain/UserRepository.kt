package pl.meleko.trainspot.domain

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.User
import pl.meleko.trainspot.requests.UpdateProfileRequest
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val myProfile: StateFlow<User?>
    suspend fun getProfile(userId: String): Result<User, DataError.Network>
    suspend fun getMyProfile(): Result<User, DataError.Network>
    suspend fun updateProfile(request: UpdateProfileRequest): Result<User, DataError.Network>
    suspend fun updateAvatar(avatarBytes: ByteArray, fileName: String, contentType: String): Result<User, DataError.Network>
    suspend fun deleteProfile(): Result<Unit, DataError.Network>
    suspend fun checkUsernameAvailability(username: String): Result<Boolean, DataError.Network>
}
