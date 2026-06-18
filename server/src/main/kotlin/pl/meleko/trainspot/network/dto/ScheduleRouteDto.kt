package pl.meleko.trainspot.network.dto

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import pl.meleko.trainspot.database.ScheduleTable
import pl.meleko.trainspot.database.TrainStopsTable
import pl.meleko.trainspot.model.ScheduleRoute
import pl.meleko.trainspot.model.StationStop
import pl.meleko.trainspot.repository.PkpRepository

@Serializable
data class SchedulesResponse(
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

suspend fun ScheduleRouteDto.toScheduleRoute() = ScheduleRoute(
    scheduleId = this.scheduleId,
    orderId = this.orderId,
    trainOrderId = this.trainOrderId,
    name = this.name,
    carrierCode = this.carrierCode,
    nationalNumber = this.name,
    internationalArrivalNumber = this.internationalArrivalNumber,
    internationalDepartureNumber = this.internationalDepartureNumber,
    commercialCategorySymbol = this.commercialCategorySymbol,
    stations = this.stations.map { it.toStationStop() }
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

suspend fun StationStopDto.toStationStop(): StationStop {
    val station = PkpRepository.getStationById(this.stationId)!!.toStation()

    return StationStop(
        stationId = station,
        orderNumber = this.orderNumber,
        departureCommercialCategory = this.arrivalCommercialCategory ?: this.departureCommercialCategory,
        departureTrainNumber = this.arrivalTrainNumber ?: this.departureTrainNumber,
        departurePlatform = this.arrivalPlatform ?: this.departurePlatform,
        departureTrack = this.arrivalTrack ?: this.departureTrack,
        departureDay = this.arrivalDay ?: this.departureDay,
        departureTime = this.arrivalTime ?: this.departureTime,
        stopTypeId = this.stopTypeId,
        stopTypeName = this.stopTypeName
    )
}

fun ResultRow.toScheduleRouteDto(): ScheduleRouteDto {
    val runId = this[ScheduleTable.trainOrderId]

    val routeStations = TrainStopsTable
        .selectAll().where { TrainStopsTable.trainRunId eq runId }
        .orderBy(TrainStopsTable.orderNumber to SortOrder.ASC)
        .map { it.toStationStopDto() }

    return ScheduleRouteDto(
        scheduleId = runId,
        orderId = this[ScheduleTable.trainOrderId].toLong(),
        trainOrderId = this[ScheduleTable.trainOrderId],
        name = this[ScheduleTable.trainName].orEmpty(),
        carrierCode = this[ScheduleTable.carrierCode].orEmpty(),
        nationalNumber = this[ScheduleTable.trainNumber].orEmpty(),
        internationalArrivalNumber = this[ScheduleTable.internationalArrivalNumber].orEmpty(),
        internationalDepartureNumber = this[ScheduleTable.internationalDepartureNumber].orEmpty(),
        commercialCategorySymbol = this[ScheduleTable.catSymbol].orEmpty(),
        stations = routeStations,
        operatingDates = listOf(this[ScheduleTable.operatingDate].toString())
    )
}

fun ResultRow.toStationStopDto() = StationStopDto(
    stationId = this[TrainStopsTable.stationId],
    orderNumber = this[TrainStopsTable.orderNumber],
    arrivalCommercialCategory = this[TrainStopsTable.arrCat].orEmpty(),
    arrivalTrainNumber = this[TrainStopsTable.arrTrainNum].orEmpty(),
    arrivalPlatform = this[TrainStopsTable.arrivalPlatform].orEmpty(),
    arrivalTrack = this[TrainStopsTable.arrivalTrack].orEmpty(),
    arrivalDay = this[TrainStopsTable.arrDayOffset] ?: 0,
    arrivalTime = this[TrainStopsTable.arrivalTime]?.toString().orEmpty(),
    departureCommercialCategory = this[TrainStopsTable.depCat].orEmpty(),
    departureTrainNumber = this[TrainStopsTable.depTrainNum].orEmpty(),
    departurePlatform = this[TrainStopsTable.platform].orEmpty(),
    departureTrack = this[TrainStopsTable.track].orEmpty(),
    departureDay = this[TrainStopsTable.depDayOffset] ?: 0,
    departureTime = this[TrainStopsTable.departureTime]?.toString().orEmpty()
)