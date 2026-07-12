package pl.meleko.trainspot.network.dto

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Alias
import org.jetbrains.exposed.v1.core.ResultRow
import pl.meleko.trainspot.database.ScheduleTable
import pl.meleko.trainspot.database.StationsTable
import pl.meleko.trainspot.database.TrainStopsTable
import pl.meleko.trainspot.model.ScheduleRoute

@Serializable
data class ScheduleRouteDto(
    val scheduleId: Int,
    val orderId: Long,
    val trainOrderId: Int,
    val name: String? = null,
    val carrierCode: String,
    val nationalNumber: String? = null,
    val commercialCategorySymbol: String,
    val originStation: StationDto? = null,
    val destStation: StationDto? = null,
    val arrivalTime: String? = null,
    val departureTime: String? = null
)

fun ScheduleRouteDto.toScheduleRoute() = ScheduleRoute(
    scheduleId = this.scheduleId,
    orderId = this.orderId,
    trainOrderId = this.trainOrderId,
    name = this.name,
    carrierCode = this.carrierCode,
    nationalNumber = this.nationalNumber,
    commercialCategorySymbol = this.commercialCategorySymbol,
    originStation = this.originStation?.toStation(),
    destStation = this.destStation?.toStation(),
    arrivalTime = this.arrivalTime,
    departureTime = this.departureTime
)

fun ResultRow.toScheduleRouteDto(
    originAlias: Alias<StationsTable>? = null,
    destAlias: Alias<StationsTable>? = null
): ScheduleRouteDto {
    val arrival = this.getOrNull(TrainStopsTable.arrivalTime)?.toString()
    val departure = this.getOrNull(TrainStopsTable.departureTime)?.toString()

    return ScheduleRouteDto(
        scheduleId = this[ScheduleTable.trainOrderId],
        orderId = this[ScheduleTable.trainOrderId].toLong(),
        trainOrderId = this[ScheduleTable.trainOrderId],
        name = this[ScheduleTable.trainName].orEmpty(),
        carrierCode = this[ScheduleTable.carrierCode].orEmpty(),
        nationalNumber = this[ScheduleTable.trainNumber].orEmpty(),
        commercialCategorySymbol = this[ScheduleTable.catSymbol].orEmpty(),
        originStation = toStationDto(originAlias),
        destStation = toStationDto(destAlias),
        arrivalTime = arrival,
        departureTime = departure
    )
}
