package pl.meleko.trainspot.data.remote

import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, DataError.Network> {
    val response = try {
        execute()
    } catch (_: UnresolvedAddressException) {
        return pl.meleko.trainspot.core.Result.Error(DataError.Network.NETWORK)
    } catch (_: SerializationException) {
        return pl.meleko.trainspot.core.Result.Error(DataError.Network.SERIALIZATION)
    } catch (_: Exception) {
        currentCoroutineContext().ensureActive()
        return Result.Error(DataError.Network.UNKNOWN)
    }

    return responseToResult(response)
}