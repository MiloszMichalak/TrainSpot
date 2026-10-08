package pl.meleko.trainspot.model

import kotlinx.serialization.Serializable

@Serializable
data class TrainModel(
    val model: String,
    val number: String? = null,
    val carrierCode: String? = null
)