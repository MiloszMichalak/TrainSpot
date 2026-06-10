package pl.meleko.trainspot.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SchedulesResponseDto(
    val generatedAt: String,
    val period: PeriodDto,
    val routes: List<ScheduleRouteDto>
)

@Serializable
data class PeriodDto(
    val from: String,
    val to: String
)

@Serializable
data class ScheduleRouteDto(
    val scheduleId: Int,
    val orderId: Long,
    val trainOrderId: Int,
    val name: String? = null,
    val carrierCode: String,
    val nationalNumber: String? = null,
    val internationalArrivalNumber: String? = null,
    val internationalDepartureNumber: String? = null,
    val commercialCategorySymbol: String,
    val operatingDates: List<String>,
    val stations: List<StationStopDto>
)

@Serializable
data class StationStopDto(
    val stationId: Int,
    val orderNumber: Int,
    val arrivalCommercialCategory: String? = null,
    val arrivalTrainNumber: String? = null,
    val arrivalPlatform: String? = null,
    val arrivalTrack: String? = null,
    val arrivalDay: Int? = null,
    val arrivalTime: String? = null,
    val departureCommercialCategory: String? = null,
    val departureTrainNumber: String? = null,
    val departurePlatform: String? = null,
    val departureTrack: String? = null,
    val departureDay: Int? = null,
    val departureTime: String? = null,
    val stopTypeId: Int? = null,
    val stopTypeName: String? = null
)