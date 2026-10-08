package pl.meleko.trainspot.repository

import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.selectAll
import pl.meleko.trainspot.database.CarriersTable
import pl.meleko.trainspot.database.CommercialCategoriesTable
import pl.meleko.trainspot.database.ScheduleTable
import pl.meleko.trainspot.database.StationsTable
import pl.meleko.trainspot.database.TrainStopsTable
import pl.meleko.trainspot.model.ScheduleRouteStops
import pl.meleko.trainspot.model.StationStop
import pl.meleko.trainspot.network.dto.CarrierDto
import pl.meleko.trainspot.network.dto.CommercialCategoryDto
import pl.meleko.trainspot.network.dto.ScheduleRouteDto
import pl.meleko.trainspot.network.dto.StationDto
import pl.meleko.trainspot.network.dto.toCarrierDto
import pl.meleko.trainspot.network.dto.toStation
import pl.meleko.trainspot.util.dbTransaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object PkpRepository {
    private val scheduleZone = ZoneId.of("Europe/Warsaw")
    suspend fun getAllStations(): List<StationDto> = dbTransaction {
        StationsTable
            .selectAll()
            .map {
                StationDto(
                    id = it[StationsTable.id].value,
                    name = it[StationsTable.name],
                    latitude = it[StationsTable.latitude],
                    longitude = it[StationsTable.longitude]
                )
            }
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

    suspend fun getTrainRouteById(trainOrderId: Int): ScheduleRouteStops? = dbTransaction {
        val route = ScheduleTable
            .selectAll()
            .where { ScheduleTable.id eq trainOrderId }
            .singleOrNull()
            ?: return@dbTransaction null

        val stopRows = TrainStopsTable
            .selectAll()
            .where { TrainStopsTable.trainRunId eq trainOrderId }
            .orderBy(TrainStopsTable.orderNumber to SortOrder.ASC)
            .toList()

        val stationIds = (stopRows.map { it[TrainStopsTable.stationId].value } +
            listOfNotNull(
                route[ScheduleTable.originStationId]?.value,
                route[ScheduleTable.destStationId]?.value
            )).distinct()

        val stations = StationsTable.selectAll()
            .where { StationsTable.id inList stationIds }
            .associate { row ->
                row[StationsTable.id].value to StationDto(
                    id = row[StationsTable.id].value,
                    name = row[StationsTable.name],
                    latitude = row[StationsTable.latitude],
                    longitude = row[StationsTable.longitude]
                ).toStation()
            }

        val stops = stopRows.map { row ->
            StationStop(
                stationId = stations.getValue(row[TrainStopsTable.stationId].value),
                orderNumber = row[TrainStopsTable.orderNumber],
                arrivalTime = row[TrainStopsTable.arrivalTime]?.toString(),
                departureCommercialCategory = row[TrainStopsTable.depCat],
                departureTrainNumber = row[TrainStopsTable.depTrainNum],
                departurePlatform = row[TrainStopsTable.platform],
                departureTrack = row[TrainStopsTable.track],
                departureDay = row[TrainStopsTable.depDayOffset],
                departureTime = row[TrainStopsTable.departureTime]?.toString()
            )
        }

        ScheduleRouteStops(
            scheduleId = route[ScheduleTable.scheduleId],
            orderId = trainOrderId.toLong(),
            trainOrderId = trainOrderId,
            name = route[ScheduleTable.trainName],
            carrierCode = route[ScheduleTable.carrierCode]?.value.orEmpty(),
            nationalNumber = route[ScheduleTable.trainNumber],
            internationalArrivalNumber = route[ScheduleTable.internationalArrivalNumber],
            internationalDepartureNumber = route[ScheduleTable.internationalDepartureNumber],
            commercialCategorySymbol = route[ScheduleTable.catSymbol],
            originStation = route[ScheduleTable.originStationId]?.value?.let(stations::get),
            destStation = route[ScheduleTable.destStationId]?.value?.let(stations::get),
            stations = stops
        )
    }

    suspend fun getRecentTrainsForStation(stationId: Int, minutes: Long): List<ScheduleRouteDto> {
        val now = LocalDateTime.now(scheduleZone)
        return fetchTrainsForStation(stationId, now.minusMinutes(minutes), now.plusMinutes(minutes))
    }

    suspend fun getTodayTrainsForStation(stationId: Int): List<ScheduleRouteDto> = fetchTrainsForStation(stationId)

    private suspend fun fetchTrainsForStation(
        stationId: Int,
        from: LocalDateTime? = null,
        to: LocalDateTime? = null
    ): List<ScheduleRouteDto> = dbTransaction {
        val stopQuery = TrainStopsTable
            .selectAll()
            .where { TrainStopsTable.stationId eq stationId }

        val matchingStops = stopQuery
            .orderBy(TrainStopsTable.departureTime to SortOrder.ASC)
            .toList()
            .groupBy { it[TrainStopsTable.trainRunId].value }

        if (matchingStops.isEmpty()) return@dbTransaction emptyList()

        val runRows = ScheduleTable
            .selectAll()
            .where {
                val stationRuns = ScheduleTable.id inList matchingStops.keys.toList()
                if (from == null) {
                    stationRuns and (ScheduleTable.operatingDate eq LocalDate.now(scheduleZone).toKotlinLocalDate())
                } else stationRuns
            }
            .toList()
            .associateBy { it[ScheduleTable.trainOrderId] }

        runRows.values.mapNotNull { run ->
            val operatingDate = run[ScheduleTable.operatingDate].toJavaLocalDate()
            val matchingTimes = matchingStops[run[ScheduleTable.trainOrderId]].orEmpty().mapNotNull stopTime@{ stop ->
                val arrival = stop[TrainStopsTable.arrivalTime]?.toJavaLocalTime()?.let {
                    operatingDate.plusDays((stop[TrainStopsTable.arrDayOffset] ?: 0).toLong()).atTime(it)
                }
                val departure = stop[TrainStopsTable.departureTime]?.toJavaLocalTime()?.let {
                    operatingDate.plusDays((stop[TrainStopsTable.depDayOffset] ?: 0).toLong()).atTime(it)
                }
                val eventTime = listOfNotNull(arrival, departure).filter {
                    from == null || to == null || (!it.isBefore(from) && !it.isAfter(to))
                }.minOrNull() ?: return@stopTime null
                stop to eventTime
            }.minByOrNull { it.second } ?: return@mapNotNull null
            Triple(run, matchingTimes.first, matchingTimes.second)
        }.sortedBy { it.third }.map { (run, times, _) ->
            ScheduleRouteDto(
                scheduleId = run[ScheduleTable.scheduleId],
                orderId = run[ScheduleTable.trainOrderId].toLong(),
                trainOrderId = run[ScheduleTable.trainOrderId],
                name = run[ScheduleTable.trainName],
                carrierCode = run[ScheduleTable.carrierCode]?.value.orEmpty(),
                nationalNumber = run[ScheduleTable.trainNumber],
                commercialCategorySymbol = run[ScheduleTable.catSymbol],
                originStation = run[ScheduleTable.originStationId]?.value?.let(::station),
                destStation = run[ScheduleTable.destStationId]?.value?.let(::station),
                arrivalTime = times[TrainStopsTable.arrivalTime]?.toString(),
                departureTime = times[TrainStopsTable.departureTime]?.toString()
            )
        }
    }

    private fun station(id: Int): StationDto? = StationsTable.selectAll().where { StationsTable.id eq id }.singleOrNull()
        ?.let {
            StationDto(
                id = it[StationsTable.id].value,
                name = it[StationsTable.name],
                latitude = it[StationsTable.latitude],
                longitude = it[StationsTable.longitude]
            )
        }
}
