package pl.meleko.trainspot.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class CommentAuthor(
    val id: Uuid,
    val username: String?,
    val avatarUrl: String?
)

@Serializable
data class Comment(
    val id: Uuid,
    val spotId: Uuid,
    val author: CommentAuthor,
    val text: String,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val likesCount: Long,
    val isLiked: Boolean
)
