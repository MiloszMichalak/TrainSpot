package pl.meleko.trainspot.domain

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.Comment
import pl.meleko.trainspot.response.PaginationResponse

interface CommentRepository {
    suspend fun getComments(spotId: String, page: Int, limit: Int): Result<PaginationResponse<Comment>, DataError.Network>
    suspend fun addComment(spotId: String, text: String): Result<Comment, DataError.Network>
    suspend fun deleteComment(spotId: String, commentId: String): Result<Unit, DataError.Network>
    suspend fun likeComment(commentId: String): Result<Unit, DataError.Network>
    suspend fun unlikeComment(commentId: String): Result<Unit, DataError.Network>
}
