package pl.meleko.trainspot.data.repository

import pl.meleko.trainspot.data.remote.CommentService
import pl.meleko.trainspot.domain.CommentRepository
import pl.meleko.trainspot.model.Comment
import pl.meleko.trainspot.response.PaginationResponse

class CommentRepositoryImpl(private val service: CommentService) : CommentRepository {
    override suspend fun getComments(spotId: String, page: Int, limit: Int) = service.getComments(spotId, page, limit)
    override suspend fun addComment(spotId: String, text: String) = service.addComment(spotId, text)
    override suspend fun deleteComment(spotId: String, commentId: String) = service.deleteComment(spotId, commentId)
    override suspend fun likeComment(commentId: String) = service.likeComment(commentId)
    override suspend fun unlikeComment(commentId: String) = service.unlikeComment(commentId)
}
