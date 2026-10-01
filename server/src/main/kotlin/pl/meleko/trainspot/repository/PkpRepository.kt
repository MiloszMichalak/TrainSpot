package pl.meleko.trainspot.repository

import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalTime
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.selectAll
import pl.meleko.trainspot.database.CarriersTable
import pl.meleko.trainspot.database.CommercialCategoriesTable
import pl.meleko.trainspot.database.ScheduleTable
import pl.meleko.trainspot.database.StationsTable
import pl.meleko.trainspot.database.TrainStopsTable
import pl.meleko.trainspot.network.dto.CarrierDto
import pl.meleko.trainspot.network.dto.CommercialCategoryDto
import pl.meleko.trainspot.network.dto.ScheduleRouteDto
import pl.meleko.trainspot.network.dto.ScheduleRouteStopsDto
import pl.meleko.trainspot.network.dto.StationDto
import pl.meleko.trainspot.network.dto.StationStopDto
import pl.meleko.trainspot.network.dto.toCarrierDto
import pl.meleko.trainspot.util.dbTransaction
import java.time.LocalDate
import java.time.LocalTime

object PkpRepository {
    suspend fun getAllStations(): List<StationDto> = dbTransaction {
        StationsTable
            .selectAll()
            .map { StationDto(it[StationsTable.id].value, it[StationsTable.name]) }
    }

    suspend fun getAllCarriers(): List<CarrierDto> = dbTransaction {
        CarriersTable
            .selectAll()
            .map { it.toCarrierDto() }
    }

    suspend fun getAllCommercialCategories(): List<CommercialCategoryDto> = dbTransaction {
        CommercialCategoriesTable
            .selectAll()
            .map {
                CommercialCategoryDto(
                    it[CommercialCategoriesTable.code],
                    it[CommercialCategoriesTable.name],
                    it[CommercialCategoriesTable.carrierCode].value
                )
            }
    }

    suspend fun getTrainRouteById(trainOrderId: Int): ScheduleRouteStopsDto = dbTransaction {
        val route = ScheduleTable
            .selectAll()
            .where { ScheduleTable.id eq trainOrderId }
            .singleOrNull()
            ?: throw NoSuchElementException("Route $trainOrderId not found")

        val carrier = CarriersTable
            .selectAll()
            .where { CarriersTable.id eq route[ScheduleTable.carrierCode].value }
            .single()

        val origin = station(route[ScheduleTable.originStationId].value)
        val dest = station(route[ScheduleTable.destStationId].value)

        val stops = TrainStopsTable
            .selectAll()
            .where { TrainStopsTable.trainRunId eq trainOrderId }
            .orderBy(TrainStopsTable.orderNumber to SortOrder.ASC)
            .map { row ->
                StationStopDto(
                    stationId = row[TrainStopsTable.stationId].value,
                    orderNumber = row[TrainStopsTable.orderNumber],
                    arrivalCommercialCategory = row[TrainStopsTable.arrCat], arrivalTrainNumber = row[TrainStopsTable.arrTrainNum],
                    arrivalPlatform = row[TrainStopsTable.arrivalPlatform], arrivalTrack = row[TrainStopsTable.arrivalTrack],
                    arrivalDay = row[TrainStopsTable.arrDayOffset], arrivalTime = row[TrainStopsTable.arrivalTime]?.toString(),
                    departureCommercialCategory = row[TrainStopsTable.depCat], departureTrainNumber = row[TrainStopsTable.depTrainNum],
                    departurePlatform = row[TrainStopsTable.platform], departureTrack = row[TrainStopsTable.track],
                    departureDay = row[TrainStopsTable.depDayOffset], departureTime = row[TrainStopsTable.departureTime]?.toString()
                )
            }

        ScheduleRouteStopsDto(
            scheduleId = route[ScheduleTable.scheduleId],
            orderId = trainOrderId.toLong(),
            trainOrderId = trainOrderId,
            name = route[ScheduleTable.trainName],
            carrierCode = carrier[CarriersTable.code],
            nationalNumber = route[ScheduleTable.trainNumber],
            internationalArrivalNumber = route[ScheduleTable.internationalArrivalNumber],
            internationalDepartureNumber = route[ScheduleTable.internationalDepartureNumber],
            commercialCategorySymbol = route[ScheduleTable.catSymbol],
            operatingDates = listOf(route[ScheduleTable.operatingDate].toString()),
            originStation = origin,
            destStation = dest,
            stations = stops
        )
    }

    suspend fun getRecentTrainsForStation(stationId: Int, minutes: Long): List<ScheduleRouteDto> {
        val now = LocalTime.now()

        val from = now.minusMinutes(minutes).toKotlinLocalTime()
        val to = now.plusMinutes(minutes).toKotlinLocalTime()

        val timeCondition = if (to < from) {
            ((TrainStopsTable.departureTime greaterEq from) or (TrainStopsTable.departureTime lessEq to)) or
                ((TrainStopsTable.arrivalTime greaterEq from) or (TrainStopsTable.arrivalTime lessEq to))
        } else {
            ((TrainStopsTable.departureTime greaterEq from) and (TrainStopsTable.departureTime lessEq to)) or
                ((TrainStopsTable.arrivalTime greaterEq from) and (TrainStopsTable.arrivalTime lessEq to))
        }

        return fetchTrainsForStation(stationId, timeCondition)
    }

    suspend fun getTodayTrainsForStation(stationId: Int): List<ScheduleRouteDto> = fetchTrainsForStation(stationId)

    private suspend fun fetchTrainsForStation(stationId: Int, timeCondition: Op<Boolean>? = null): List<ScheduleRouteDto> = dbTransaction {
        val stopQuery = TrainStopsTable
            .selectAll()
            .where {
                val base = (TrainStopsTable.stationId eq stationId)
                if (timeCondition != null) base and timeCondition else base
            }

        val matchingStops = stopQuery
            .orderBy(TrainStopsTable.departureTime to SortOrder.ASC)
            .toList()
            .groupBy { it[TrainStopsTable.trainRunId].value }

        val runRows = ScheduleTable
            .selectAll()
            .where {
                (ScheduleTable.id inList matchingStops.keys.toList()) and
                    (ScheduleTable.operatingDate eq LocalDate.now().toKotlinLocalDate())
            }
            .toList()
            .associateBy { it[ScheduleTable.trainOrderId] }

        runRows.values.sortedBy { run ->
            matchingStops[run[ScheduleTable.trainOrderId]]?.firstOrNull()?.get(TrainStopsTable.departureTime)
        }.mapNotNull { run ->
            val times = matchingStops[run[ScheduleTable.trainOrderId]]?.firstOrNull() ?: return@mapNotNull null
            val carrier = CarriersTable
                .selectAll()
                .where { CarriersTable.id eq run[ScheduleTable.carrierCode].value }
                .singleOrNull() ?: return@mapNotNull null

            ScheduleRouteDto(
                scheduleId = run[ScheduleTable.scheduleId],
                orderId = run[ScheduleTable.trainOrderId].toLong(),
                trainOrderId = run[ScheduleTable.trainOrderId],
                name = run[ScheduleTable.trainName],
                carrierCode = carrier[CarriersTable.code],
                nationalNumber = run[ScheduleTable.trainNumber],
                commercialCategorySymbol = run[ScheduleTable.catSymbol],
                originStation = station(run[ScheduleTable.originStationId].value),
                destStation = station(run[ScheduleTable.destStationId].value),
                arrivalTime = times[TrainStopsTable.arrivalTime]?.toString(),
                departureTime = times[TrainStopsTable.departureTime]?.toString()
            )
        }
    }

    private fun station(id: Int): StationDto? = StationsTable.selectAll().where { StationsTable.id eq id }.singleOrNull()
        ?.let { StationDto(it[StationsTable.id].value, it[StationsTable.name]) }
}
