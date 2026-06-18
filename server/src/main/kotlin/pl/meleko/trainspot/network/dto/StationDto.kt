package pl.meleko.trainspot.network.dto

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ResultRow
import pl.meleko.trainspot.database.StationsTable

@Serializable
data class StationsResponse(
    val stations: List<StationDto>
)

@Serializable
data class StationDto(
    val id: Int,
    val name: String
)

fun ResultRow.toStationDto() = StationDto(
    id = this[StationsTable.id].value,
    name = this[StationsTable.name]
)