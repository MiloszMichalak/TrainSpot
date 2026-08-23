package pl.meleko.trainspot.database.model

import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.network.dto.ScheduleRouteDto
import pl.meleko.trainspot.network.dto.StationDto
import pl.meleko.trainspot.network.dto.toScheduleRoute
import pl.meleko.trainspot.network.dto.toStation
import java.time.OffsetDateTime
import kotlin.time.Instant
import kotlin.time.toKotlinInstant
import kotlin.uuid.Uuid

data class SpotDto(
    val id: Uuid,
    val user: UserDto,
    val model: TrainModelDto,
    val station: StationDto?,
    val trainRun: ScheduleRouteDto,
    val imageUrl: String,
    val description: String?,
    val lat: Double?,
    val lon: Double?,
    val spottedAt: OffsetDateTime,
    val createdAt: Instant,
    val likesCount: Long = 0
)

fun SpotDto.toSpotResponse(): Spot {
    return Spot(
        id = this.id,
        user = this.user.toUser(),
        model = this.model.toTrainModel(),
        station = this.station?.toStation(),
        trainRun = this.trainRun.toScheduleRoute(),
        imageUrl = this.imageUrl,
        description = this.description.orEmpty(),
        lat = this.lat,
        lon = this.lon,
        spottedAt = this.spottedAt.toInstant().toKotlinInstant(),
        createdAt = this.createdAt,
        likes = this.likesCount
    )
}