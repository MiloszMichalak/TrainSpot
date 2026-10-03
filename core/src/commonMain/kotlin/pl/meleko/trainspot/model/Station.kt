package pl.meleko.trainspot.model

import kotlinx.serialization.Serializable

@Serializable
data class Station(
    val id: Int,
    val name: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)