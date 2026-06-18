package pl.meleko.trainspot.repository

import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalTime
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
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
import pl.meleko.trainspot.network.dto.StationDto
import pl.meleko.trainspot.network.dto.toCarrierDto
import pl.meleko.trainspot.network.dto.toCommercialCategoryDto
import pl.meleko.trainspot.network.dto.toScheduleRouteDto
import pl.meleko.trainspot.network.dto.toStationDto
import pl.meleko.trainspot.util.dbTransaction
import java.time.LocalDate
import java.time.LocalTime

object PkpRepository {
    suspend fun getAllStations(): List<StationDto> {
        return dbTransaction {
            StationsTable.selectAll().map { row ->
                row.toStationDto()
            }
        }
    }

    suspend fun getAllCarriers(): List<CarrierDto> {
        return dbTransaction {
            CarriersTable.selectAll().map { row ->
                row.toCarrierDto()
            }
        }
    }

    suspend fun getAllCommercialCategories(): List<CommercialCategoryDto> {
        return dbTransaction {
            CommercialCategoriesTable.selectAll().map { row ->
                row.toCommercialCategoryDto()
            }
        }
    }

    suspend fun getRecentTrainsForStation(stationId: Int, minutes: Long): List<ScheduleRouteDto> {
        val originStation = StationsTable.alias("origin_station")

        return dbTransaction {
            (TrainStopsTable innerJoin ScheduleTable innerJoin StationsTable)
                .join(originStation, JoinType.LEFT, ScheduleTable.originStationId, originStation[StationsTable.id])
                .selectAll()
                .where {
                    (TrainStopsTable.stationId eq stationId) and
                            (ScheduleTable.operatingDate eq LocalDate.now().toKotlinLocalDate()) and
                            (
                                    (TrainStopsTable.departureTime greaterEq LocalTime.now().minusMinutes(minutes).toKotlinLocalTime()) or
                                            (TrainStopsTable.arrivalTime greaterEq LocalTime.now().minusMinutes(minutes).toKotlinLocalTime())
                                    )
                }
                .orderBy(TrainStopsTable.departureTime to SortOrder.ASC)
                .toList()
                .map { it.toScheduleRouteDto() }
        }
    }

    suspend fun getTodayTrainsForStation(stationId: Int): List<ScheduleRouteDto> {
        val originStation = StationsTable.alias("origin_station")
        val destStation = StationsTable.alias("dest_station")

        return dbTransaction {
            (TrainStopsTable innerJoin ScheduleTable innerJoin StationsTable)
                .join(originStation, JoinType.LEFT, ScheduleTable.originStationId, originStation[StationsTable.id])
                .join(destStation, JoinType.LEFT, ScheduleTable.destStationId, destStation[StationsTable.id])
                .selectAll()
                .where {
                    (TrainStopsTable.stationId eq stationId) and
                            (ScheduleTable.operatingDate eq LocalDate.now().toKotlinLocalDate())
                }
                .orderBy(TrainStopsTable.departureTime to SortOrder.ASC)
                .toList()
                .map { it.toScheduleRouteDto() }
        }
    }
}
