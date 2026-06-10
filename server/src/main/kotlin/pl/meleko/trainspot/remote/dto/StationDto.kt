package pl.meleko.trainspot.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class StationsResponseDto(
    val stations: List<StationDto>
)

@Serializable
data class StationDto(
    val id: Int,
    val name: String
)