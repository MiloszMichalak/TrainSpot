package pl.meleko.trainspot.data.repository

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.data.remote.UserService
import pl.meleko.trainspot.domain.UserRepository
import pl.meleko.trainspot.model.User
import pl.meleko.trainspot.requests.UpdateProfileRequest
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.meleko.trainspot.core.onSuccess

class UserRepositoryImpl(
    private val userService: UserService
) : UserRepository {
    private val _myProfile = MutableStateFlow<User?>(null)
    override val myProfile: StateFlow<User?> = _myProfile.asStateFlow()

    private fun publishProfile(user: User): User {
        _myProfile.value = user
        return user
    }

    override suspend fun getProfile(userId: String): Result<User, DataError.Network> {
        return userService.getProfile(userId)
    }

    override suspend fun getMyProfile(): Result<User, DataError.Network> {
        return userService.getMyProfile().also { result ->
            result.onSuccess { publishProfile(it) }
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<User, DataError.Network> {
        return userService.updateProfile(request).also { result -> result.onSuccess { publishProfile(it) } }
    }

    override suspend fun updateAvatar(avatarBytes: ByteArray, fileName: String, contentType: String): Result<User, DataError.Network> {
        return userService.updateAvatar(avatarBytes, fileName, contentType).also { result -> result.onSuccess { publishProfile(it) } }
    }

    override suspend fun deleteProfile(): Result<Unit, DataError.Network> {
        return userService.deleteProfile()
    }

    override suspend fun checkUsernameAvailability(username: String): Result<Boolean, DataError.Network> {
        return userService.checkUsernameAvailability(username)
    }
}
