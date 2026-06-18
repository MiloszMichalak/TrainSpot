package pl.meleko.trainspot.database.model

import org.jetbrains.exposed.v1.core.ResultRow
import pl.meleko.trainspot.database.SpotsTable
import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.model.TrainModel
import java.time.OffsetDateTime
import kotlin.time.Instant
import kotlin.time.toKotlinInstant
import kotlin.uuid.Uuid

data class SpotDto(
    val id: Uuid,
    val userId: Uuid,
    val modelId: Uuid?,
    val stationId: Int?,
    val trainNumber: String?,
    val trainRunId: Int?,
    val imageUrl: String,
    val description: String?,
    val lat: Double?,
    val lon: Double?,
    val spottedAt: OffsetDateTime,
    val createdAt: Instant,
    val user: UserDto? = null,
    val model: TrainModel? = null,
    val likesCount: Int = 0
) {
    fun toSpotResponse(): Spot {
        return Spot(
            id = this.id,
            user = user,
            model = model,
            stationId = this.stationId,
            trainRunId = this.trainRunId ?: 0,
            imageUrl = this.imageUrl,
            description = this.description.orEmpty(),
            lat = this.lat,
            lon = this.lon,
            spottedAt = this.spottedAt.toInstant().toKotlinInstant(),
            createdAt = this.createdAt,
            likes = this.likesCount
        )
    }
}

fun ResultRow.toSpotDto() = SpotDto(
    id = this[SpotsTable.id].value,
    userId = this[SpotsTable.userId],
    modelId = this[SpotsTable.modelId],
    stationId = this[SpotsTable.stationId],
    trainNumber = this[SpotsTable.trainNumber],
    trainRunId = this[SpotsTable.trainRunId],
    imageUrl = this[SpotsTable.imageUrl],
    description = this[SpotsTable.description],
    lat = this[SpotsTable.lat],
    lon = this[SpotsTable.lon],
    spottedAt = this[SpotsTable.spottedAt],
    createdAt = this[SpotsTable.createdAt]
)