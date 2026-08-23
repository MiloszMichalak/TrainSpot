package pl.meleko.trainspot.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class SchedulesResponse(
    val generatedAt: String,
    val period: PeriodDto,
    val routes: List<ScheduleRouteStopsDto>
)

@Serializable
data class PeriodDto(
    val from: String,
    val to: String
)

@Serializable
data class ScheduleRouteStopsDto(
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
    val originStation: StationDto? = null,
    val destStation: StationDto? = null,
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

//fun ScheduleRouteStopsDto.toScheduleRouteStops() = ScheduleRouteStops(
//    scheduleId = this.scheduleId,
//    orderId = this.orderId,
//    trainOrderId = this.trainOrderId,
//    name = this.name,
//    carrierCode = this.carrierCode,
//    nationalNumber = this.nationalNumber,
//    internationalArrivalNumber = this.internationalArrivalNumber,
//    internationalDepartureNumber = this.internationalDepartureNumber,
//    commercialCategorySymbol = this.commercialCategorySymbol,
//    stations = this.stations.map { it.toStationStop() }
//)
//
//fun StationStopDto.toStationStop() = StationStop(
//    stationId = Station(this.stationId, ""),
//    orderNumber = this.orderNumber,
//    departureCommercialCategory = this.arrivalCommercialCategory ?: this.departureCommercialCategory,
//    departureTrainNumber = this.arrivalTrainNumber ?: this.departureTrainNumber,
//    departurePlatform = this.arrivalPlatform ?: this.departurePlatform,
//    departureTrack = this.arrivalTrack ?: this.departureTrack,
//    departureDay = this.arrivalDay ?: this.departureDay,
//    arrivalTime = this.arrivalTime,
//    departureTime = this.departureTime,
//    stopTypeId = this.stopTypeId,
//    stopTypeName = this.stopTypeName
//)