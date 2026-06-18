package pl.meleko.trainspot.requests

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class SpotRequest(
    val modelId: Uuid?,
    val stationId: Int?,
    val trainRunId: Int?,
    val description: String?,
    val lat: Double?,
    val lon: Double?
)