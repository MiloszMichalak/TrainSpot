package pl.meleko.trainspot.database.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class StationGeolocation(
    @JsonNames("plkId") val id: Int,
    val latitude: Double,
    val longitude: Double
)