package pl.meleko.trainspot.util

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext

context(routing: RoutingContext)
suspend inline fun <reified T : Any> NetworkResult<T>.mapToResponse() {
    return when (this) {
        is NetworkResult.Success ->  routing.call.respond(HttpStatusCode.OK, this.data)
        is NetworkResult.Error -> routing.call.respond(this.error)
    }
}
