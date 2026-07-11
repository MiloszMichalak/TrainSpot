package pl.meleko.trainspot.data.repository

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.core.onSuccess
import pl.meleko.trainspot.data.local.AuthDataStore
import pl.meleko.trainspot.data.remote.AuthService
import pl.meleko.trainspot.domain.repository.AuthRepository
import pl.meleko.trainspot.response.AuthResponse

class AuthRepositoryImpl(
    private val apiService: AuthService,
    private val authDataStore: AuthDataStore,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResponse, DataError.Network> {
        return apiService.login(email, password).onSuccess {
            authDataStore.saveToken(it.token)
            authDataStore.saveRefreshToken(it.refreshToken)
        }
    }

    override suspend fun register(email: String, password: String): Result<AuthResponse, DataError.Network> {
        return apiService.register(email, password).onSuccess {
            authDataStore.saveToken(it.token)
            authDataStore.saveRefreshToken(it.refreshToken)
        }
    }

    override suspend fun checkSession(): Result<AuthResponse, DataError.Network> {
        return apiService.checkSession()
    }

    override suspend fun refreshTokens(): Result<AuthResponse, DataError.Network> {
        val refreshToken = authDataStore.getRefreshToken() ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        return apiService.refresh(refreshToken).onSuccess {
            authDataStore.saveToken(it.token)
            authDataStore.saveRefreshToken(it.refreshToken)
        }
    }

    override suspend fun getToken() = authDataStore.getToken()

    override suspend fun getRefreshToken() = authDataStore.getRefreshToken()

    override suspend fun logout() = authDataStore.clearToken()
}