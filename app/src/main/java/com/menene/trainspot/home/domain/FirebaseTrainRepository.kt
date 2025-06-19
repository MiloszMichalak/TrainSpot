package com.menene.trainspot.home.domain

interface FirebaseTrainRepository {
    suspend fun uploadTrainImage(
        bytes: ByteArray
    )

    suspend fun getTrainImage(
        imageUri: String
    ): String
}