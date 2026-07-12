package pl.meleko.trainspot.repository

import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalTime
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.selectAll
import pl.meleko.trainspot.database.CarriersTable
import pl.meleko.trainspot.database.CommercialCategoriesTable
import pl.meleko.trainspot.database.DbAliases
import pl.meleko.trainspot.database.ScheduleTable
import pl.meleko.trainspot.database.StationsTable
import pl.meleko.trainspot.database.TrainStopsTable
import pl.meleko.trainspot.network.dto.CarrierDto
import pl.meleko.trainspot.network.dto.CommercialCategoryDto
import pl.meleko.trainspot.network.dto.ScheduleRouteDto
import pl.meleko.trainspot.network.dto.ScheduleRouteStopsDto
import pl.meleko.trainspot.network.dto.StationDto
import pl.meleko.trainspot.network.dto.toCarrierDto
import pl.meleko.trainspot.network.dto.toCommercialCategoryDto
import pl.meleko.trainspot.network.dto.toScheduleRouteDto
import pl.meleko.trainspot.network.dto.toScheduleRouteStopsDto
import pl.meleko.trainspot.network.dto.toStationDto
import pl.meleko.trainspot.network.dto.toStationStopDto
import pl.meleko.trainspot.util.dbTransaction
import java.time.LocalDate
import java.time.LocalTime

object PkpRepository {
    suspend fun getAllStations(): List<StationDto> {
        return dbTransaction {
            StationsTable.selectAll().mapNotNull { row ->
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

    suspend fun getTrainRouteById(trainOrderId: Int): ScheduleRouteStopsDto {
        return dbTransaction {
            val routeRow = ScheduleTable
                .join(
                    DbAliases.originStation,
                    JoinType.LEFT,
                    ScheduleTable.originStationId,
                    DbAliases.originStation[StationsTable.id]
                )
                .join(
                    DbAliases.destStation,
                    JoinType.LEFT,
                    ScheduleTable.destStationId,
                    DbAliases.destStation[StationsTable.id]
                )
                .selectAll()
                .where { ScheduleTable.trainOrderId eq trainOrderId }
                .first()

            val stops = TrainStopsTable.selectAll()
                .where { TrainStopsTable.trainRunId eq trainOrderId }
                .orderBy(TrainStopsTable.orderNumber to SortOrder.ASC)
                .map { it.toStationStopDto() }

            routeRow.toScheduleRouteStopsDto(stops, DbAliases.originStation, DbAliases.destStation)
        }
    }

    suspend fun getRecentTrainsForStation(stationId: Int, minutes: Long): List<ScheduleRouteDto> {
        val now = LocalTime.now()
        val from = now.minusMinutes(minutes).toKotlinLocalTime()
        val to = now.plusMinutes(minutes).toKotlinLocalTime()

        val isMidnightCrossed = to < from

        return dbTransaction {
            (TrainStopsTable innerJoin ScheduleTable)
                .join(
                    DbAliases.originStation,
                    JoinType.LEFT,
                    ScheduleTable.originStationId,
                    DbAliases.originStation[StationsTable.id]
                )
                .join(
                    DbAliases.destStation,
                    JoinType.LEFT,
                    ScheduleTable.destStationId,
                    DbAliases.destStation[StationsTable.id]
                )
                .selectAll()
                .where {
                    val timeCondition = if (isMidnightCrossed) {
                        (
                                (TrainStopsTable.departureTime greaterEq from) or
                                        (TrainStopsTable.departureTime lessEq to)
                                ) or (
                                (TrainStopsTable.arrivalTime greaterEq from) or
                                        (TrainStopsTable.arrivalTime lessEq to)
                                )
                    } else {
                        (
                                (TrainStopsTable.departureTime greaterEq from) and
                                        (TrainStopsTable.departureTime lessEq to)
                                ) or (
                                (TrainStopsTable.arrivalTime greaterEq from) and
                                        (TrainStopsTable.arrivalTime lessEq to)
                                )
                    }

                    (TrainStopsTable.stationId eq stationId) and
                            (ScheduleTable.operatingDate eq LocalDate.now().toKotlinLocalDate()) and
                            timeCondition
                }
                .orderBy(TrainStopsTable.departureTime to SortOrder.ASC)
                .map { it.toScheduleRouteDto(DbAliases.originStation, DbAliases.destStation) }
        }
    }

    suspend fun getTodayTrainsForStation(stationId: Int): List<ScheduleRouteDto> {
        return dbTransaction {
            (TrainStopsTable innerJoin ScheduleTable)
                .join(
                    DbAliases.originStation,
                    JoinType.LEFT,
                    ScheduleTable.originStationId,
                    DbAliases.originStation[StationsTable.id]
                )
                .join(
                    DbAliases.destStation,
                    JoinType.LEFT,
                    ScheduleTable.destStationId,
                    DbAliases.destStation[StationsTable.id]
                )
                .selectAll()
                .where {
                    (TrainStopsTable.stationId eq stationId) and
                            (ScheduleTable.operatingDate eq LocalDate.now().toKotlinLocalDate())
                }
                .orderBy(TrainStopsTable.departureTime to SortOrder.ASC)
                .map { it.toScheduleRouteDto(DbAliases.originStation, DbAliases.destStation) }
        }
    }
}
