package com.menene.trainspot.data

import com.menene.trainspot.util.Error

enum class DataError: Error {
    INVALID_DATA,
    USER_ALREADY_EXISTS,
    TOO_MANY_REQUESTS,
    NETWORK_ERROR,
    UNKNOWN_ERROR;

//    override fun toString(): String {
//        return when (this) {
//            INVALID_EMAIL -> "Invalid email format"
//            INVALID_PASSWORD -> "Password does not meet requirements"
//            USER_NOT_FOUND -> "User not found"
//            USER_ALREADY_EXISTS -> "User already exists"
//            NETWORK_ERROR -> "Network error occurred"
//            UNKNOWN_ERROR -> "An unknown error occurred"
//        }
//    }
}