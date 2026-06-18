package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
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
import pl.meleko.trainspot.util.toInstant
import pl.meleko.trainspot.util.toLocalDate
import pl.meleko.trainspot.util.toLocalTime

object PkpImportRepository {
    fun importStations(stations: List<StationDto>) {
        return transaction {
            stations.forEach { station ->
                StationsTable.insert {
                    it[id] = station.id
                    it[name] = station.name
                    it[fetchedAt] = CurrentTimestamp
                }
            }
        }
    }

    fun importCarriersData(carriers: List<CarrierDto>) {
        return transaction {
            carriers.forEach { carrier ->
                CarriersTable.insert {
                    it[code] = carrier.code
                    it[name] = carrier.name
                    it[validFrom] = carrier.validFrom.toInstant()
                    it[validTo] = carrier.validTo?.toInstant()
                }
            }
        }
    }

    fun importCommercialCategories(categories: List<CommercialCategoryDto>) {
        return transaction {
            categories.forEach { category ->
                CommercialCategoriesTable.insert {
                    it[code] = category.code
                    it[name] = category.name
                    it[carrierCode] = category.carrierCode
                }
            }
        }
    }

    fun importSchedule(trainRun: ScheduleRouteDto) {
        ScheduleTable.insert {
            it[scheduleId] = trainRun.scheduleId
            it[trainOrderId] = trainRun.trainOrderId
            it[trainName] = trainRun.name
            it[carrierCode] = trainRun.carrierCode
            it[trainNumber] = trainRun.nationalNumber
            it[catSymbol] = trainRun.commercialCategorySymbol
            it[operatingDate] = trainRun.operatingDates.first().toLocalDate()
            it[internationalArrivalNumber] = trainRun.internationalArrivalNumber
            it[internationalDepartureNumber] = trainRun.internationalDepartureNumber
            it[originStationId] = trainRun.stations.first().stationId
            it[destStationId] = trainRun.stations.last().stationId
        }
    }

    fun importTrainStops(runId: Int, stops: List<StationStopDto>) {
        stops.forEach { stop ->
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

    fun clearDictionariesData() {
        transaction {
            StationsTable.deleteAll()
            CommercialCategoriesTable.deleteAll()
            CarriersTable.deleteAll()
        }
    }
}
