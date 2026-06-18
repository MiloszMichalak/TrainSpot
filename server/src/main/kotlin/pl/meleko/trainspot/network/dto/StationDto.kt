package pl.meleko.trainspot.network.dto

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ResultRow
import pl.meleko.trainspot.database.StationsTable
import pl.meleko.trainspot.model.Station

@Serializable
data class StationsResponse(
    val stations: List<StationDto>
)

@Serializable
data class StationDto(
    val id: Int,
    val name: String
)

fun StationDto.toStation() = Station(
    id = this.id,
    name = this.name
)

fun ResultRow.toStationDto() = StationDto(
    id = this[StationsTable.id].value,
    name = this[StationsTable.name]
)