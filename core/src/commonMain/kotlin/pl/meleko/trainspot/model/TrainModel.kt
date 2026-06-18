package pl.meleko.trainspot.model

import kotlinx.serialization.Serializable

@Serializable
data class TrainModel(
    val model: String,
    val number: String,
    val carrierCode: String
)