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
    val lon: Double?,
    val manualTrainNumber: String? = null,
    val originStationId: Int? = null,
    val destinationStationId: Int? = null
)

/** A manual route has either both endpoints or neither; its number identifies the train. */
fun SpotRequest.hasValidTrainSelection(): Boolean {
    val number = manualTrainNumber?.trim()?.takeIf { it.isNotEmpty() }
    val hasEndpoints = originStationId != null || destinationStationId != null
    return (originStationId == null) == (destinationStationId == null) &&
        (!hasEndpoints || number != null) &&
        (number == null || (number.length <= 50 && trainRunId == null && stationId != null))
}
