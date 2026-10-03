package pl.meleko.trainspot.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
import pl.meleko.trainspot.database.CarriersTable
import pl.meleko.trainspot.database.CommercialCategoriesTable
import pl.meleko.trainspot.database.ScheduleTable
import pl.meleko.trainspot.database.StationsTable
import pl.meleko.trainspot.database.TrainStopsTable
import pl.meleko.trainspot.database.model.StationGeolocation
import pl.meleko.trainspot.network.dto.CarrierDto
import pl.meleko.trainspot.network.dto.CommercialCategoryDto
import pl.meleko.trainspot.network.dto.ScheduleRouteStopsDto
import pl.meleko.trainspot.network.dto.StationDto
import pl.meleko.trainspot.network.dto.StationStopDto
import pl.meleko.trainspot.util.dbTransaction
import pl.meleko.trainspot.util.toInstant
import pl.meleko.trainspot.util.toLocalDate
import pl.meleko.trainspot.util.toLocalTime
import java.io.File
import java.time.LocalDate

object PkpImportRepository {
    private val geolocationJson = Json { ignoreUnknownKeys = true }

    suspend fun withStationGeolocation(
        stations: List<StationDto>,
        file: File = File("../specs/stations.json")
    ): List<StationDto> = withContext(Dispatchers.IO) {
        val coordinatesById = geolocationJson
            .decodeFromString<List<StationGeolocation>>(file.readText(Charsets.UTF_8))
            .associateBy { it.id }

        stations.map { station ->
            val coordinates = coordinatesById[station.id]
            if (coordinates == null) station else station.copy(
                latitude = coordinates.latitude,
                longitude = coordinates.longitude
            )
        }
    }

    suspend fun importStations(stations: List<StationDto>) {
        return dbTransaction {
            stations.forEach { station ->
                StationsTable.insert {
                    it[id] = station.id
                    it[name] = station.name
                    it[latitude] = station.latitude
                    it[longitude] = station.longitude
                    it[fetchedAt] = CurrentTimestamp
                }
            }
        }
    }

    suspend fun importCarriersData(carriers: List<CarrierDto>) {
        return dbTransaction {
            carriers.forEach { carrier ->
                CarriersTable.insert {
                    it[id] = carrier.code
                    it[name] = carrier.name
                    it[validFrom] = carrier.validFrom.toInstant()
                    it[validTo] = carrier.validTo?.toInstant()
                }
            }
        }
    }

    suspend fun importCommercialCategories(categories: List<CommercialCategoryDto>) {
        return dbTransaction {
            categories.forEach { category ->
                CommercialCategoriesTable.insert {
                    it[code] = category.code
                    it[name] = category.name
                    it[carrierCode] = category.carrierCode
                }
            }
        }
    }

    suspend fun importSchedule(trainRun: ScheduleRouteStopsDto) {
        val sortedStops = trainRun.stations.sortedBy { it.orderNumber }
        dbTransaction {
            ScheduleTable.insert {
                it[id] = trainRun.trainOrderId
                it[scheduleId] = trainRun.scheduleId
                it[trainName] = trainRun.name
                it[carrierCode] = trainRun.carrierCode
                it[trainNumber] = trainRun.nationalNumber
                it[catSymbol] = trainRun.commercialCategorySymbol
                it[operatingDate] = trainRun.operatingDates.firstOrNull()?.toLocalDate() ?: LocalDate.now().toKotlinLocalDate()
                it[internationalArrivalNumber] = trainRun.internationalArrivalNumber
                it[internationalDepartureNumber] = trainRun.internationalDepartureNumber
                it[originStationId] = sortedStops.first().stationId
                it[destStationId] = sortedStops.last().stationId
            }
        }
    }

    suspend fun importTrainStops(runId: Int, stops: List<StationStopDto>) {
        stops.forEach { stop ->
            dbTransaction {
                TrainStopsTable.insert {
                    it[trainRunId] = runId
                    it[stationId] = stop.stationId
                    it[orderNumber] = stop.orderNumber
                    it[arrCat] = stop.arrivalCommercialCategory
                    it[arrTrainNum] = stop.arrivalTrainNumber
                    it[arrivalPlatform] = stop.arrivalPlatform
                    it[arrivalTrack] = stop.arrivalTrack
                    it[arrDayOffset] = stop.arrivalDay
                    it[arrivalTime] = stop.arrivalTime.toLocalTime()
                    it[depCat] = stop.departureCommercialCategory
                    it[depTrainNum] = stop.departureTrainNumber
                    it[platform] = stop.departurePlatform
                    it[track] = stop.departureTrack
                    it[departureTime] = stop.departureTime.toLocalTime()
                    it[depDayOffset] = stop.departureDay
                }
            }
        }
    }

    suspend fun clearDictionariesData() {
        dbTransaction {
            StationsTable.deleteAll()
            CommercialCategoriesTable.deleteAll()
            CarriersTable.deleteAll()
        }
    }
}
