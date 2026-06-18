package pl.meleko.trainspot.requests

import kotlinx.serialization.Serializable
import pl.meleko.trainspot.model.TrainModel

@Serializable
data class SpotRequest(
    val trainModel: TrainModel?,
    val stationId: Int?,
    val trainRunId: Int?,
    val description: String?,
    val lat: Double?,
    val lon: Double?
)