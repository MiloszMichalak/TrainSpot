package pl.meleko.trainspot.service

import io.ktor.http.HttpStatusCode
import pl.meleko.trainspot.model.Comment
import pl.meleko.trainspot.repository.CommentLikeCreation
import pl.meleko.trainspot.repository.CommentLikesRepository
import pl.meleko.trainspot.repository.CommentsRepository
import pl.meleko.trainspot.response.PaginationResponse
import pl.meleko.trainspot.util.NetworkResult
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object CommentsService {
    private const val MAX_COMMENT_LENGTH = 2000

    suspend fun findBySpot(spotId: Uuid, viewerId: Uuid, page: Int, limit: Int): NetworkResult<PaginationResponse<Comment>> {
        if (page < 0 || limit < 1 || limit > 100) return NetworkResult.Error(HttpStatusCode.BadRequest)
        if (!CommentsRepository.spotExists(spotId)) return NetworkResult.Error(HttpStatusCode.NotFound)
        return NetworkResult.Success(CommentsRepository.findBySpot(spotId, viewerId, page, limit))
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun create(spotId: Uuid, userId: Uuid, text: String): NetworkResult<Comment> {
        val normalized = text.trim()
        if (!validText(normalized)) return NetworkResult.Error(HttpStatusCode.BadRequest)
        val commentId = Uuid.generateV4()
        val comment = CommentsRepository.create(spotId, userId, normalized, commentId)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        return NetworkResult.Success(comment)
    }

    suspend fun update(commentId: Uuid, spotId: Uuid, userId: Uuid, text: String): NetworkResult<Comment> {
        val normalized = text.trim()
        if (!validText(normalized)) return NetworkResult.Error(HttpStatusCode.BadRequest)
        val existing = CommentsRepository.findById(commentId, userId, spotId)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        if (existing.author.id != userId) return NetworkResult.Error(HttpStatusCode.Forbidden)
        if (!CommentsRepository.update(commentId, userId, normalized)) return NetworkResult.Error(HttpStatusCode.NotFound)
        return CommentsRepository.findById(commentId, userId, spotId)
            ?.let { NetworkResult.Success(it) }
            ?: NetworkResult.Error(HttpStatusCode.NotFound)
    }

    suspend fun delete(commentId: Uuid, spotId: Uuid, userId: Uuid): NetworkResult<Unit> {
        val existing = CommentsRepository.findById(commentId, userId, spotId)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        if (existing.author.id != userId) return NetworkResult.Error(HttpStatusCode.Forbidden)
        return if (CommentsRepository.delete(commentId, userId)) {
            NetworkResult.Success(Unit)
        } else {
            NetworkResult.Error(HttpStatusCode.NotFound)
        }
    }

    suspend fun like(commentId: Uuid, userId: Uuid): NetworkResult<Unit> = when (CommentLikesRepository.create(commentId, userId)) {
        CommentLikeCreation.CREATED -> NetworkResult.Success(Unit)
        CommentLikeCreation.ALREADY_EXISTS -> NetworkResult.Error(HttpStatusCode.Conflict)
        CommentLikeCreation.COMMENT_NOT_FOUND -> NetworkResult.Error(HttpStatusCode.NotFound)
    }

    suspend fun unlike(commentId: Uuid, userId: Uuid): NetworkResult<Unit> {
        if (CommentsRepository.findById(commentId, userId) == null) return NetworkResult.Error(HttpStatusCode.NotFound)
        return if (CommentLikesRepository.delete(commentId, userId)) {
            NetworkResult.Success(Unit)
        } else {
            NetworkResult.Error(HttpStatusCode.NotFound)
        }
    }

    private fun validText(text: String): Boolean = text.isNotEmpty() && text.length <= MAX_COMMENT_LENGTH
}
