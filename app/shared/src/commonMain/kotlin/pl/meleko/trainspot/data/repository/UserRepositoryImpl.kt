package pl.meleko.trainspot.data.repository

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.data.remote.UserService
import pl.meleko.trainspot.domain.repository.UserRepository
import pl.meleko.trainspot.model.User
import pl.meleko.trainspot.requests.UpdateProfileRequest

class UserRepositoryImpl(
    private val userService: UserService
) : UserRepository {
    override suspend fun getProfile(userId: String): Result<User, DataError.Network> {
        return userService.getProfile(userId)
    }

    override suspend fun getMyProfile(): Result<User, DataError.Network> {
        return userService.getMyProfile()
    }

    override suspend fun updateProfile(request: UpdateProfileRequest, avatarBytes: ByteArray?): Result<User, DataError.Network> {
        return userService.updateProfile(request, avatarBytes)
    }

    override suspend fun deleteProfile(): Result<Unit, DataError.Network> {
        return userService.deleteProfile()
    }

    override suspend fun checkUsernameAvailability(username: String): Result<Boolean, DataError.Network> {
        return userService.checkUsernameAvailability(username)
    }
}
