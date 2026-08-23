package pl.meleko.trainspot.database.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class LikeDto(
    val id: Uuid,
    val userId: Uuid,
    val spotId: Uuid,
    val createdAt: Instant
)
