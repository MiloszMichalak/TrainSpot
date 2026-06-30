package pl.meleko.trainspot.domain.repository

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.User
import pl.meleko.trainspot.requests.UpdateProfileRequest

interface UserRepository {
    suspend fun getProfile(userId: String): Result<User, DataError.Network>
    suspend fun getMyProfile(): Result<User, DataError.Network>
    suspend fun updateProfile(request: UpdateProfileRequest, avatarBytes: ByteArray?): Result<User, DataError.Network>
    suspend fun deleteProfile(): Result<Unit, DataError.Network>
    suspend fun checkUsernameAvailability(username: String): Result<Boolean, DataError.Network>
}
