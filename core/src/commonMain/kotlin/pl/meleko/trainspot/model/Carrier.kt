package pl.meleko.trainspot.model

import kotlinx.serialization.Serializable

@Serializable
data class Carrier(
    val code: String,
    val name: String,
    val validFrom: String,
    val validTo: String?
)
