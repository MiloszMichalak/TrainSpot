package com.menene.trainspot.home.data

import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.menene.trainspot.home.domain.FirebaseTrainRepository
import com.menene.trainspot.util.Error
import com.menene.trainspot.util.Result
import com.menene.trainspot.util.safeFirebaseCall
import kotlinx.coroutines.tasks.await

class FirebaseTrainRepositoryImpl(
    private val trainStorage: TrainStorage
): FirebaseTrainRepository{
    override suspend fun getTrainNames(): Result<List<String>, Error> {
        return safeFirebaseCall {
            val list = Firebase.database.reference
                .child("trains")
                .get()
                .await()
                .children
                .mapNotNull {
                    it.getValue<String>()
                }

            trainStorage.setList(list)
            list
        }
    }

    override suspend fun uploadTrainImage(bytes: ByteArray): Result<Unit, Error> {
        return safeFirebaseCall {
            // TODO w liscie musza byc numer pociagu i jaki to jest pociag
            Firebase.storage.reference.child("images").putBytes(bytes).await()
        }
    }

    override suspend fun getTrainImage(imageUri: String): Result<Unit, Error> {
        return safeFirebaseCall {

        }
    }

}