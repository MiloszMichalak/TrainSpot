package pl.meleko.trainspot.database.model

import org.jetbrains.exposed.v1.core.ResultRow
import pl.meleko.trainspot.SpotResponse
import pl.meleko.trainspot.database.SpotsTable
import java.time.OffsetDateTime
import kotlin.time.Instant
import kotlin.time.toKotlinInstant
import kotlin.uuid.Uuid

data class SpotDto(
    val id: Uuid,
    val userId: Uuid,
    val modelId: Uuid?,
    val stationId: Int?,
    val trainRunId: Int?,
    val imageUrl: String,
    val description: String?,
    val lat: Double?,
    val lon: Double?,
    val spottedAt: OffsetDateTime,
    val createdAt: Instant,
//    val likes: Int TODO ni ma ale w spocie co leci do usera juz beda
)

fun SpotDto.toSpotResponse(
    user: UserDto,
    likes: Long
) = SpotResponse(
    id = this.id,
    user = user.toUser(),
    modelId = this.modelId,
    stationId = this.stationId,
    trainRunId = this.trainRunId,
    imageUrl = this.imageUrl,
    description = this.description,
    lat = this.lat,
    lon = this.lon,
    spottedAt = this.spottedAt.toInstant().toKotlinInstant(),
    createdAt = this.createdAt,
    likes = likes.toInt()
)

fun ResultRow.toSpotDto() = SpotDto(
    id = this[SpotsTable.id].value,
    userId = this[SpotsTable.userId],
    modelId = this[SpotsTable.modelId],
    stationId = this[SpotsTable.stationId],
    trainRunId = this[SpotsTable.trainRunId],
    imageUrl = this[SpotsTable.imageUrl],
    description = this[SpotsTable.description],
    lat = this[SpotsTable.lat],
    lon = this[SpotsTable.lon],
    spottedAt = this[SpotsTable.spottedAt],
    createdAt = this[SpotsTable.createdAt]
)