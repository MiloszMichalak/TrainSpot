package pl.meleko.trainspot.model

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleRouteStops(
    val scheduleId: Int,
    val orderId: Long,
    val trainOrderId: Int,
    val name: String? = null,
    val carrierCode: String,
    val nationalNumber: String? = null,
    val internationalArrivalNumber: String? = null,
    val internationalDepartureNumber: String? = null,
    val commercialCategorySymbol: String,
    val originStation: Station? = null,
    val destStation: Station? = null,
    val stations: List<StationStop>
)
