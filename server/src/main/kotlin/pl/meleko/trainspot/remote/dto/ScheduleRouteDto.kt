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
    val name: String,
    val carrierCode: String,
    val nationalNumber: String,
    val internationalArrivalNumber: String,
    val internationalDepartureNumber: String,
    val commercialCategorySymbol: String,
    val operatingDates: List<String>,
    val stations: List<StationStopDto>
)

@Serializable
data class StationStopDto(
    val stationId: Int,
    val orderNumber: Int,
    val arrivalCommercialCategory: String,
    val arrivalTrainNumber: String,
    val arrivalPlatform: String,
    val arrivalTrack: String,
    val arrivalDay: Int,
    val arrivalTime: String,
    val departureCommercialCategory: String,
    val departureTrainNumber: String,
    val departurePlatform: String,
    val departureTrack: String,
    val departureDay: Int,
    val departureTime: String
)