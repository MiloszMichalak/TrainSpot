package pl.meleko.trainspot.database.entities

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import pl.meleko.trainspot.database.TrainStopsTable
import pl.meleko.trainspot.network.dto.StationStopDto

class TrainStopEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TrainStopEntity>(TrainStopsTable)

    var trainRun by ScheduleEntity referencedOn TrainStopsTable.trainRunId
    var station by StationEntity referencedOn TrainStopsTable.stationId
    var orderNumber by TrainStopsTable.orderNumber
    var arrCat by TrainStopsTable.arrCat
    var arrTrainNum by TrainStopsTable.arrTrainNum
    var arrivalPlatform by TrainStopsTable.arrivalPlatform
    var arrivalTrack by TrainStopsTable.arrivalTrack
    var arrDayOffset by TrainStopsTable.arrDayOffset
    var arrivalTime by TrainStopsTable.arrivalTime
    var depCat by TrainStopsTable.depCat
    var depTrainNum by TrainStopsTable.depTrainNum
    var platform by TrainStopsTable.platform
    var track by TrainStopsTable.track
    var departureTime by TrainStopsTable.departureTime
    var depDayOffset by TrainStopsTable.depDayOffset

    fun toDto() = StationStopDto(
        stationId = station.id.value,
        orderNumber = orderNumber,
        arrivalCommercialCategory = arrCat,
        arrivalTrainNumber = arrTrainNum,
        arrivalPlatform = arrivalPlatform,
        arrivalTrack = arrivalTrack,
        arrivalDay = arrDayOffset,
        arrivalTime = arrivalTime?.toString(),
        departureCommercialCategory = depCat,
        departureTrainNumber = depTrainNum,
        departurePlatform = platform,
        departureTrack = track,
        departureDay = depDayOffset,
        departureTime = departureTime?.toString()
    )
}
