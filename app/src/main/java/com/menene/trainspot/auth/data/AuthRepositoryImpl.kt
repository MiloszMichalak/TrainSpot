package com.menene.trainspot.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.menene.trainspot.auth.domain.repository.AuthRepository
import com.menene.trainspot.util.DataError
import com.menene.trainspot.util.Result
import com.menene.trainspot.util.safeFirebaseCall
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
): AuthRepository {
    override suspend fun login(
        email: String,
        password: String
    ): Result<Unit, DataError> {
        return safeFirebaseCall {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        }
    }

    override suspend fun register(
        email: String,
        password: String
    ): Result<Unit, DataError> {
        return safeFirebaseCall {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        }
    }

    override suspend fun logout(): Result<Unit, DataError> {
        return safeFirebaseCall {
            firebaseAuth.signOut()
        }
    }
}