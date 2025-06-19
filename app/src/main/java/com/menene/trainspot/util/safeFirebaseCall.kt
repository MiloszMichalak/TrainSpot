package com.menene.trainspot.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.menene.trainspot.util.DataError

suspend inline fun <T> safeFirebaseCall(
    crossinline block: suspend ()  -> T
): Result<T, DataError> {
    return try {
        block()
        Result.Success(block())
    } catch (e: FirebaseAuthUserCollisionException) {
        Result.Error(DataError.USER_ALREADY_EXISTS)
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Result.Error(DataError.INVALID_DATA)
    } catch (e: FirebaseTooManyRequestsException) {
        Result.Error(DataError.TOO_MANY_REQUESTS)
    } catch (e: FirebaseNetworkException) {
        Result.Error(DataError.NETWORK_ERROR)
    } catch (e: Exception) {
        Result.Error(DataError.UNKNOWN_ERROR)
    }
}
