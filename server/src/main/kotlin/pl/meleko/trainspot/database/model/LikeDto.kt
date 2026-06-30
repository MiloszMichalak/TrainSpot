package pl.meleko.trainspot.database.model

import org.jetbrains.exposed.v1.core.ResultRow
import pl.meleko.trainspot.database.LikesTable
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class LikeDto(
    val id: Uuid,
    val userId: Uuid,
    val spotId: Uuid,
    val createdAt: Instant
)

fun ResultRow.toLikeDto() = LikeDto(
    id = this[LikesTable.id].value,
    userId = this[LikesTable.userId],
    spotId = this[LikesTable.spotId],
    createdAt = this[LikesTable.createdAt]
)
