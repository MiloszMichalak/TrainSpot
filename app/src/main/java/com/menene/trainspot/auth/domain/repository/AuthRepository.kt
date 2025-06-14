package com.menene.trainspot.auth.domain.repository

import com.menene.trainspot.auth.data.DataError
import com.menene.trainspot.util.Result

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit, DataError>
    suspend fun register(email: String, password: String): Result<Unit, DataError>
    suspend fun logout(): Result<Unit, DataError>
}