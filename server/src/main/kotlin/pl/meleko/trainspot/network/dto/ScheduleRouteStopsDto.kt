package pl.meleko.trainspot.network.dto

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Alias
import org.jetbrains.exposed.v1.core.ResultRow
import pl.meleko.trainspot.database.ScheduleTable
import pl.meleko.trainspot.database.StationsTable
import pl.meleko.trainspot.database.TrainStopsTable

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

fun ResultRow.toScheduleRouteStopsDto(
    stations: List<StationStopDto>,
    originAlias: Alias<StationsTable>? = null,
    destAlias: Alias<StationsTable>? = null
): ScheduleRouteStopsDto {
    return ScheduleRouteStopsDto(
        scheduleId = this[ScheduleTable.trainOrderId],
        orderId = this[ScheduleTable.trainOrderId].toLong(),
        trainOrderId = this[ScheduleTable.trainOrderId],
        name = this[ScheduleTable.trainName].orEmpty(),
        carrierCode = this[ScheduleTable.carrierCode].orEmpty(),
        nationalNumber = this[ScheduleTable.trainNumber].orEmpty(),
        internationalArrivalNumber = this[ScheduleTable.internationalArrivalNumber].orEmpty(),
        internationalDepartureNumber = this[ScheduleTable.internationalDepartureNumber].orEmpty(),
        commercialCategorySymbol = this[ScheduleTable.catSymbol].orEmpty(),
        stations = stations,
        operatingDates = listOf(this[ScheduleTable.operatingDate].toString()),
        originStation = toStationDto(originAlias),
        destStation = toStationDto(destAlias)
    )
}

fun ResultRow.toStationStopDto() = StationStopDto(
    stationId = this[TrainStopsTable.stationId],
    orderNumber = this[TrainStopsTable.orderNumber],
    arrivalCommercialCategory = this[TrainStopsTable.arrCat],
    arrivalTrainNumber = this[TrainStopsTable.arrTrainNum],
    arrivalPlatform = this[TrainStopsTable.arrivalPlatform],
    arrivalTrack = this[TrainStopsTable.arrivalTrack],
    arrivalDay = this[TrainStopsTable.arrDayOffset],
    arrivalTime = this[TrainStopsTable.arrivalTime]?.toString(),
    departureCommercialCategory = this[TrainStopsTable.depCat],
    departureTrainNumber = this[TrainStopsTable.depTrainNum],
    departurePlatform = this[TrainStopsTable.platform],
    departureTrack = this[TrainStopsTable.track],
    departureDay = this[TrainStopsTable.depDayOffset],
    departureTime = this[TrainStopsTable.departureTime]?.toString()
)
