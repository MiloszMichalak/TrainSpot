package pl.meleko.trainspot.database.model

import org.jetbrains.exposed.v1.core.Alias
import org.jetbrains.exposed.v1.core.ResultRow
import pl.meleko.trainspot.database.SpotsTable
import pl.meleko.trainspot.database.StationsTable
import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.network.dto.ScheduleRouteDto
import pl.meleko.trainspot.network.dto.StationDto
import pl.meleko.trainspot.network.dto.toScheduleRoute
import pl.meleko.trainspot.network.dto.toScheduleRouteDto
import pl.meleko.trainspot.network.dto.toStation
import pl.meleko.trainspot.network.dto.toStationDto
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
    val likesCount: Int = 0
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

fun ResultRow.toSpotDto(
    originAlias: Alias<StationsTable>? = null,
    destAlias: Alias<StationsTable>? = null
) = SpotDto(
    id = this[SpotsTable.id].value,
    user = this.toUserDto(),
    model = this.toTrainModelDto(),
    station = this.toStationDto(),
    trainRun = this.toScheduleRouteDto(originAlias, destAlias),
    imageUrl = this[SpotsTable.imageUrl],
    description = this[SpotsTable.description],
    lat = this[SpotsTable.lat],
    lon = this[SpotsTable.lon],
    spottedAt = this[SpotsTable.spottedAt],
    createdAt = this[SpotsTable.createdAt]
)