package pl.meleko.trainspot.util

import io.ktor.http.HttpStatusCode

sealed interface NetworkResult<out D> {
    data class Success<out D>(val data: D) : NetworkResult<D>
    data class Error(val error: HttpStatusCode) : NetworkResult<Nothing>
}

typealias EmptyResult = NetworkResult<Unit>

inline fun <T, R> NetworkResult<T>.map(
    map: (T) -> R
): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Error -> this
        is NetworkResult.Success -> NetworkResult.Success(map(data))
    }
}

inline fun <T> NetworkResult<T>.onSuccess(
    action: (T) -> Unit
): NetworkResult<T> {
    return when (this) {
        is NetworkResult.Error -> this
        is NetworkResult.Success -> {
            action(data)
            this
        }
    }
}

inline fun <T> NetworkResult<T>.onFailure(
    action: (HttpStatusCode) -> Unit
): NetworkResult<T> {
    return when (this) {
        is NetworkResult.Error -> {
            action(error)
            this
        }
        is NetworkResult.Success -> this
    }
}

fun <T> NetworkResult<T>.asEmptyResult(): EmptyResult {
    return map { }
}
