package pl.meleko.trainspot.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.Comment
import pl.meleko.trainspot.requests.CommentRequest
import pl.meleko.trainspot.response.PaginationResponse

class CommentService(private val client: HttpClient) {
    suspend fun getComments(spotId: String, page: Int, limit: Int): Result<PaginationResponse<Comment>, DataError.Network> = safeCall {
        client.get("/spot/$spotId/comments") {
            parameter("page", page)
            parameter("limit", limit)
        }
    }

    suspend fun addComment(spotId: String, text: String): Result<Comment, DataError.Network> = safeCall {
        client.post("/spot/$spotId/comments") {
            contentType(ContentType.Application.Json)
            setBody(CommentRequest(text))
        }
    }

    suspend fun deleteComment(spotId: String, commentId: String): Result<Unit, DataError.Network> = safeCall {
        client.delete("/spot/$spotId/comments/$commentId")
    }

    suspend fun likeComment(commentId: String): Result<Unit, DataError.Network> = safeCall {
        client.post("/likes/comments/$commentId")
    }

    suspend fun unlikeComment(commentId: String): Result<Unit, DataError.Network> = safeCall {
        client.delete("/likes/comments/$commentId")
    }
}
