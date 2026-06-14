package pl.meleko.trainspot.repository

import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalTime
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import pl.meleko.trainspot.database.CarriersTable
import pl.meleko.trainspot.database.CommercialCategoriesTable
import pl.meleko.trainspot.database.ScheduleTable
import pl.meleko.trainspot.database.StationsTable
import pl.meleko.trainspot.database.TrainStopsTable
import pl.meleko.trainspot.network.dto.CarrierDto
import pl.meleko.trainspot.network.dto.CommercialCategoryDto
import pl.meleko.trainspot.network.dto.ScheduleRouteDto
import pl.meleko.trainspot.network.dto.StationDto
import pl.meleko.trainspot.network.dto.StationStopDto
import pl.meleko.trainspot.util.toDateString
import java.time.LocalDate
import java.time.LocalTime

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

    fun getRecentTrainsForStation(stationId: Int): List<ResultRow> {
        val originStation = StationsTable.alias("origin_station")

        return transaction {
            (TrainStopsTable innerJoin ScheduleTable innerJoin StationsTable)
                .join(originStation, JoinType.LEFT, ScheduleTable.originStationId, originStation[StationsTable.id])
                .select(
                    ScheduleTable.trainNumber,
                    ScheduleTable.trainName,
                    ScheduleTable.originStationId,
                    originStation[StationsTable.name],
                    StationsTable.name,
                    TrainStopsTable.arrivalTime,
                    TrainStopsTable.departureTime,
                )
                .where {
                    (TrainStopsTable.stationId eq stationId) and
                            (ScheduleTable.operatingDate eq LocalDate.now().toKotlinLocalDate()) and
                            (
                                    (TrainStopsTable.departureTime greaterEq LocalTime.now().minusHours(3).toKotlinLocalTime()) or
                                            (TrainStopsTable.arrivalTime greaterEq LocalTime.now().minusHours(3).toKotlinLocalTime())
                                    )
                }
                .orderBy(TrainStopsTable.departureTime to SortOrder.ASC)
                .toList()
        }
    }

    fun getTodayTrainsForStation(stationId: Int): List<ResultRow> {
        val originStation = StationsTable.alias("origin_station")
        val destStation = StationsTable.alias("dest_station")

        return transaction {
            (TrainStopsTable innerJoin ScheduleTable innerJoin StationsTable)
                .join(originStation, JoinType.LEFT, ScheduleTable.originStationId, originStation[StationsTable.id])
                .join(destStation, JoinType.LEFT, ScheduleTable.destStationId, destStation[StationsTable.id])
                .select(
                    ScheduleTable.trainNumber,
                    ScheduleTable.trainName,
                    ScheduleTable.operatingDate,
                    originStation[StationsTable.name],
                    destStation[StationsTable.name],
                    TrainStopsTable.arrivalTime,
                    TrainStopsTable.departureTime,
                    TrainStopsTable.arrivalPlatform
                )
                .where {
                    (TrainStopsTable.stationId eq stationId) and
                            (ScheduleTable.operatingDate eq LocalDate.now().toKotlinLocalDate())
                }
                .orderBy(TrainStopsTable.departureTime to SortOrder.ASC)
                .toList()
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
    val runId = this[ScheduleTable.trainOrderId]

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
