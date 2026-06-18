package pl.meleko.trainspot.model

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleRoute(
    val scheduleId: Int,
    val orderId: Long,
    val trainOrderId: Int,
    val name: String? = null,
    val carrierCode: String,
    val nationalNumber: String? = null,
    val internationalArrivalNumber: String? = null,
    val internationalDepartureNumber: String? = null,
    val commercialCategorySymbol: String,
    val stations: List<StationStop>
)