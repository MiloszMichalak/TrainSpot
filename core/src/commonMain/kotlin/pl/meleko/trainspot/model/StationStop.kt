package pl.meleko.trainspot.model

import kotlinx.serialization.Serializable

// todo usuniete arrival zeby w bazie danych po prostu je do jednego skracac
@Serializable
data class StationStop(
    val stationId: Station,
    val orderNumber: Int,
    val departureCommercialCategory: String? = null,
    val departureTrainNumber: String? = null,
    val departurePlatform: String? = null,
    val departureTrack: String? = null,
    val departureDay: Int? = null,
    val departureTime: String? = null,
    val stopTypeId: Int? = null,
    val stopTypeName: String? = null
)