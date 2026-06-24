package pl.meleko.trainspot.core

sealed interface DataError: Error {
    enum class Network: DataError {
        SERVICE_UNAVAILABLE,
        CLIENT_ERROR,
        SERVER_ERROR,
        SERIALIZATION,
        UNKNOWN,
        UNAUTHORIZED,
        NOT_FOUND,
        CONFLICT,
        INVALID_DATA,
        NETWORK
    }
    enum class Local: DataError {
        DISK_FULL,
        UNKNOWN
    }
}
