package pl.meleko.trainspot.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CarriersResponseDto(
    val generatedAt: String,
    val carriers: List<CarrierDto>,
)

@Serializable
data class CarrierDto(
    val code: String,
    val name: String,
    val validFrom: String,
    val validTo: String?
)
