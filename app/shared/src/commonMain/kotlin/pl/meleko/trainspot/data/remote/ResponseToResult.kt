package pl.meleko.trainspot.data.remote

import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result

suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): Result<T, DataError.Network> {
    return when (response.status.value) {
        in 200..299 -> {
            try {
                Result.Success(response.body<T>())
            } catch (_: NoTransformationFoundException) {
                Result.Error(DataError.Network.SERIALIZATION)
            }
        }
        401 -> Result.Error(DataError.Network.UNAUTHORIZED)
        404 -> Result.Error(DataError.Network.NOT_FOUND)
        409 -> Result.Error(DataError.Network.CONFLICT)
        422 -> Result.Error(DataError.Network.INVALID_DATA)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
}
