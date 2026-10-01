package pl.meleko.trainspot.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import pl.meleko.trainspot.requests.CommentRequest
import pl.meleko.trainspot.service.CommentsService
import pl.meleko.trainspot.util.jwtUserId
import pl.meleko.trainspot.util.mapToResponse
import kotlin.uuid.Uuid

fun Routing.installCommentRoutes() {
    authenticate("auth-jwt") {
        route("/spot/{spotId}/comments") {
            get {
                val spotId = call.parameters["spotId"]?.let { runCatching { Uuid.parse(it) }.getOrNull() }
                if (spotId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val pageValue = call.request.queryParameters["page"]
                val limitValue = call.request.queryParameters["limit"]
                val page = pageValue?.toIntOrNull() ?: if (pageValue == null) 0 else null
                val limit = limitValue?.toIntOrNull() ?: if (limitValue == null) 20 else null
                if (page == null || limit == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                CommentsService.findBySpot(spotId, call.jwtUserId(), page, limit).mapToResponse()
            }

            post {
                val spotId = call.parameters["spotId"]?.let { runCatching { Uuid.parse(it) }.getOrNull() }
                if (spotId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                when (val result = CommentsService.create(spotId, call.jwtUserId(), call.receive<CommentRequest>().text)) {
                    is pl.meleko.trainspot.util.NetworkResult.Success -> call.respond(HttpStatusCode.Created, result.data)
                    is pl.meleko.trainspot.util.NetworkResult.Error -> call.respond(result.error)
                }
            }

            put("/{commentId}") {
                val spotId = call.parameters["spotId"]?.let { runCatching { Uuid.parse(it) }.getOrNull() }
                val commentId = call.parameters["commentId"]?.let { runCatching { Uuid.parse(it) }.getOrNull() }
                if (spotId == null || commentId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@put
                }
                CommentsService.update(commentId, spotId, call.jwtUserId(), call.receive<CommentRequest>().text).mapToResponse()
            }

            delete("/{commentId}") {
                val spotId = call.parameters["spotId"]?.let { runCatching { Uuid.parse(it) }.getOrNull() }
                val commentId = call.parameters["commentId"]?.let { runCatching { Uuid.parse(it) }.getOrNull() }
                if (spotId == null || commentId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }
                when (val result = CommentsService.delete(commentId, spotId, call.jwtUserId())) {
                    is pl.meleko.trainspot.util.NetworkResult.Success -> call.respond(HttpStatusCode.NoContent)
                    is pl.meleko.trainspot.util.NetworkResult.Error -> call.respond(result.error)
                }
            }
        }
    }
}
