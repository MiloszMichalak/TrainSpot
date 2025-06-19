package com.menene.trainspot.util

import com.menene.trainspot.R

enum class DataError: Error {
    INVALID_DATA,
    USER_ALREADY_EXISTS,
    TOO_MANY_REQUESTS,
    NETWORK_ERROR,
    UNKNOWN_ERROR;

    override fun toIntType(): Int {
        return when (this) {
            INVALID_DATA -> R.string.invalid_data
            USER_ALREADY_EXISTS -> R.string.user_exists
            TOO_MANY_REQUESTS -> R.string.too_many_requests
            NETWORK_ERROR -> R.string.network_error
            UNKNOWN_ERROR -> R.string.unknown_error
        }
    }
}