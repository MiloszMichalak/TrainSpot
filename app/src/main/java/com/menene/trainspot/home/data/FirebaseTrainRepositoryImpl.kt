package com.menene.trainspot.home.data

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.menene.trainspot.home.domain.FirebaseTrainRepository
import com.menene.trainspot.util.safeFirebaseCall
import kotlinx.coroutines.tasks.await

class FirebaseTrainRepositoryImpl: FirebaseTrainRepository{
    override suspend fun uploadTrainImage(bytes: ByteArray) {
        safeFirebaseCall {
            Firebase.storage.reference.child("images").putBytes(bytes).await()
        }
    }

    override suspend fun getTrainImage(imageUri: String): String {
        return "yes" //TODO wiadomo
    }

}