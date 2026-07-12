package pl.meleko.trainspot.network.dto

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Alias
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

fun ResultRow.toStationDto(alias: Alias<StationsTable>? = null): StationDto? {
    val id = if (alias != null) {
        this.getOrNull(alias[StationsTable.id])?.value
    } else {
        this.getOrNull(StationsTable.id)?.value
    }

    val name = if (alias != null) {
        this.getOrNull(alias[StationsTable.name])
    } else {
        this.getOrNull(StationsTable.name)
    }

    if (id == null || name == null) return null

    return StationDto(
        id = id,
        name = name
    )
}