package pl.meleko.trainspot.network.dto

import kotlinx.serialization.Serializable
import pl.meleko.trainspot.model.Station

@Serializable
data class StationsResponse(
    val stations: List<StationDto>
)

@Serializable
data class StationDto(
    val id: Int,
    val name: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

fun StationDto.toStation() = Station(
    id = this.id,
    name = this.name,
    latitude = this.latitude,
    longitude = this.longitude
)