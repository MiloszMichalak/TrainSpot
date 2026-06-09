package pl.meleko.trainspot.remote

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import pl.meleko.trainspot.database.CarriersTable
import pl.meleko.trainspot.database.CommercialCategoriesTable
import pl.meleko.trainspot.database.ScheduleTable
import pl.meleko.trainspot.database.StationsTable
import pl.meleko.trainspot.database.TrainStopsTable
import pl.meleko.trainspot.remote.dto.CarrierDto
import pl.meleko.trainspot.remote.dto.CommercialCategoryDto
import pl.meleko.trainspot.remote.dto.ScheduleRouteDto
import pl.meleko.trainspot.remote.dto.StationDto
import pl.meleko.trainspot.remote.dto.StationStopDto
import pl.meleko.trainspot.util.toDateString

object PkpRepository {
    fun getAllStations(): List<StationDto> {
        return transaction {
            StationsTable.selectAll().map { row ->
                row.toPkpStationDto()
            }
        }
    }

    fun getAllCarriers(): List<CarrierDto> {
        return transaction {
            CarriersTable.selectAll().map { row ->
                row.toPkpCarrierDto()
            }
        }
    }

    fun getAllCommercialCategories(): List<CommercialCategoryDto> {
        return transaction {
            CommercialCategoriesTable.selectAll().map { row ->
                row.toPkpCommercialCategoryDto()
            }
        }
    }

    fun getLastTrainsForStation(stationId: Int, limit: Int = 3): List<ScheduleRouteDto> {
        return transaction {
            ScheduleTable
                .selectAll().where {
                    ScheduleTable.id eq stationId
                }
                .orderBy(
                    order = SortOrder.DESC,
                    column = ScheduleTable.operatingDate
                )
                .take(limit)
                .map { row -> row.toScheduleRouteDto() }
        }
    }

    fun getAllTrainsForStation(stationId: Int): List<ScheduleRouteDto> {
        return transaction {
            ScheduleTable
                .selectAll().where {
                    (ScheduleTable.originStationId eq stationId) or (ScheduleTable.destStationId eq stationId)
                }
                .map { row -> row.toScheduleRouteDto() }
        }
    }
}

fun ResultRow.toPkpStationDto() = StationDto(
    id = this[StationsTable.id].value,
    name = this[StationsTable.name]
)

fun ResultRow.toPkpCarrierDto() = CarrierDto(
    code = this[CarriersTable.code],
    name = this[CarriersTable.name],
    validFrom = this[CarriersTable.validFrom].toDateString(),
    validTo = this[CarriersTable.validTo].toDateString()
)

fun ResultRow.toPkpCommercialCategoryDto() = CommercialCategoryDto(
    code = this[CommercialCategoriesTable.code],
    name = this[CommercialCategoriesTable.name].orEmpty(),
    carrierCode = this[CommercialCategoriesTable.carrierCode]
)

fun ResultRow.toScheduleRouteDto(): ScheduleRouteDto {
    val runId = this[ScheduleTable.id].value

    val routeStations = TrainStopsTable
        .selectAll().where { TrainStopsTable.trainRunId eq runId }
        .orderBy(TrainStopsTable.orderNumber to SortOrder.ASC)
        .map { it.toStationStopDto() }

    return ScheduleRouteDto(
        scheduleId = runId,
        orderId = this[ScheduleTable.trainOrderId] as Long,
        trainOrderId = this[ScheduleTable.trainOrderId],
        name = this[ScheduleTable.trainName].orEmpty(),
        carrierCode = this[ScheduleTable.carrierCode].orEmpty(),
        nationalNumber = this[ScheduleTable.trainNumber].orEmpty(),
        internationalArrivalNumber = this[ScheduleTable.internationalArrivalNumber].orEmpty(),
        internationalDepartureNumber = this[ScheduleTable.internationalDepartureNumber].orEmpty(),
        commercialCategorySymbol = this[ScheduleTable.catSymbol].orEmpty(),
        stations = routeStations,
        operatingDates = listOf(this[ScheduleTable.operatingDate].toDateString())
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
