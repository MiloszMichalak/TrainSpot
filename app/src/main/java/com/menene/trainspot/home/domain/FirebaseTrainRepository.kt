package com.menene.trainspot.home.domain

import com.menene.trainspot.util.Error
import com.menene.trainspot.util.Result

interface FirebaseTrainRepository {
    suspend fun getTrainNames(): Result<List<String>, Error>

    suspend fun uploadTrainImage(
        bytes: ByteArray
    ): Result<Unit, Error>

    suspend fun getTrainImage(
        imageUri: String
    ): Result<Unit, Error>
}