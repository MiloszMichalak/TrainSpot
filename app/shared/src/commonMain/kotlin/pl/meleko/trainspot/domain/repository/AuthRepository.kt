package pl.meleko.trainspot.domain.repository

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.User
import pl.meleko.trainspot.response.AuthResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse, DataError.Network>
    suspend fun register(email: String, password: String): Result<AuthResponse, DataError.Network>
    suspend fun updateUsername(username: String): Result<User, DataError.Network>
    suspend fun getToken(): String?
    suspend fun logout()
}
