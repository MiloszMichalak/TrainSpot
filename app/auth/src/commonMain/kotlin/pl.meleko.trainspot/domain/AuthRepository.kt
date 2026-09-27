package pl.meleko.trainspot.domain

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.response.AuthResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse, DataError.Network>
    suspend fun register(email: String, password: String): Result<AuthResponse, DataError.Network>
    suspend fun checkSession(): Result<AuthResponse, DataError.Network>
    suspend fun refreshTokens(): Result<AuthResponse, DataError.Network>
    suspend fun getToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun logout()
}