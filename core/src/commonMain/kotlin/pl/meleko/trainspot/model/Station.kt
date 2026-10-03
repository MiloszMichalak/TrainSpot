package pl.meleko.trainspot.model

import kotlinx.serialization.Serializable

@Serializable
data class Station(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double
)