package pl.meleko.trainspot.database.entities

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import pl.meleko.trainspot.database.ScheduleTable
import pl.meleko.trainspot.database.TrainStopsTable
import pl.meleko.trainspot.network.dto.ScheduleRouteDto
import pl.meleko.trainspot.network.dto.ScheduleRouteStopsDto

class ScheduleEntity(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, ScheduleEntity>(ScheduleTable)

    var scheduleId by ScheduleTable.scheduleId
    var trainName by ScheduleTable.trainName
    var carrier by CarrierEntity referencedOn ScheduleTable.carrierCode
    var trainNumber by ScheduleTable.trainNumber
    var catSymbol by ScheduleTable.catSymbol
    var operatingDate by ScheduleTable.operatingDate
    var internationalArrivalNumber by ScheduleTable.internationalArrivalNumber
    var internationalDepartureNumber by ScheduleTable.internationalDepartureNumber
    var originStation by StationEntity referencedOn ScheduleTable.originStationId
    var destStation by StationEntity referencedOn ScheduleTable.destStationId

    val stops by TrainStopEntity referrersOn TrainStopsTable.trainRunId

    fun toDto(arrivalTime: String? = null, departureTime: String? = null) = ScheduleRouteDto(
        scheduleId = scheduleId,
        orderId = id.value.toLong(),
        trainOrderId = id.value,
        name = trainName,
        carrierCode = carrier.id.value,
        nationalNumber = trainNumber,
        commercialCategorySymbol = catSymbol,
        originStation = originStation.toDto(),
        destStation = destStation.toDto(),
        arrivalTime = arrivalTime,
        departureTime = departureTime
    )

    fun toStopsDto() = ScheduleRouteStopsDto(
        scheduleId = scheduleId,
        orderId = id.value.toLong(),
        trainOrderId = id.value,
        name = trainName,
        carrierCode = carrier.id.value,
        nationalNumber = trainNumber,
        internationalArrivalNumber = internationalArrivalNumber,
        internationalDepartureNumber = internationalDepartureNumber,
        commercialCategorySymbol = catSymbol,
        operatingDates = listOf(operatingDate.toString()),
        originStation = originStation.toDto(),
        destStation = destStation.toDto(),
        stations = stops.map { it.toDto() }
    )
}
