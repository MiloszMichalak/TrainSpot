package pl.meleko.trainspot.model

import kotlinx.serialization.Serializable

@Serializable
data class CommercialCategory(
    val code: String,
    val name: String,
    val carrierCode: String?
)
