package pl.meleko.trainspot.repository

import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalTime
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.select
import pl.meleko.trainspot.database.ScheduleTable
import pl.meleko.trainspot.database.TrainStopsTable
import pl.meleko.trainspot.database.entities.CarrierEntity
import pl.meleko.trainspot.database.entities.CommercialCategoryEntity
import pl.meleko.trainspot.database.entities.ScheduleEntity
import pl.meleko.trainspot.database.entities.StationEntity
import pl.meleko.trainspot.network.dto.CarrierDto
import pl.meleko.trainspot.network.dto.CommercialCategoryDto
import pl.meleko.trainspot.network.dto.ScheduleRouteDto
import pl.meleko.trainspot.network.dto.ScheduleRouteStopsDto
import pl.meleko.trainspot.network.dto.StationDto
import pl.meleko.trainspot.util.dbTransaction
import java.time.LocalDate
import java.time.LocalTime

object PkpRepository {
    suspend fun getAllStations(): List<StationDto> {
        return dbTransaction {
            StationEntity.all().map { it.toDto() }
        }
    }

    suspend fun getAllCarriers(): List<CarrierDto> {
        return dbTransaction {
            CarrierEntity.all().map { it.toDto() }
        }
    }

    suspend fun getAllCommercialCategories(): List<CommercialCategoryDto> {
        return dbTransaction {
            CommercialCategoryEntity.all().map { it.toDto() }
        }
    }

    suspend fun getTrainRouteById(trainOrderId: Int): ScheduleRouteStopsDto {
        return dbTransaction {
            ScheduleEntity.findById(trainOrderId)
                ?.toStopsDto()
                ?: throw NoSuchElementException("Route $trainOrderId not found")
        }
    }

    suspend fun getRecentTrainsForStation(stationId: Int, minutes: Long): List<ScheduleRouteDto> {
        val now = LocalTime.now()
        val from = now.minusMinutes(minutes).toKotlinLocalTime()
        val to = now.plusMinutes(minutes).toKotlinLocalTime()

        val isMidnightCrossed = to < from

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

        return fetchTrainsForStation(stationId, timeCondition)
    }

    suspend fun getTodayTrainsForStation(stationId: Int): List<ScheduleRouteDto> {
        return fetchTrainsForStation(stationId)
    }

    private suspend fun fetchTrainsForStation(
        stationId: Int,
        timeCondition: Op<Boolean>? = null
    ): List<ScheduleRouteDto> {
        return dbTransaction {
            val query = (TrainStopsTable innerJoin ScheduleTable)
                .select(ScheduleTable.columns + TrainStopsTable.arrivalTime + TrainStopsTable.departureTime)
                .where {
                    val baseCondition = (TrainStopsTable.stationId eq stationId) and
                            (ScheduleTable.operatingDate eq LocalDate.now().toKotlinLocalDate())

                    if (timeCondition != null) {
                        baseCondition and timeCondition
                    } else {
                        baseCondition
                    }
                }
                .orderBy(TrainStopsTable.departureTime to SortOrder.ASC)

            ScheduleEntity.wrapRows(query)
                .with(
                    ScheduleEntity::carrier,
                    ScheduleEntity::originStation,
                    ScheduleEntity::destStation,
                    ScheduleEntity::stops
                )
                .map {
                    val (arrivalTime, departureTime) = getTimesForStation(it, stationId)
                    it.toDto(arrivalTime, departureTime)
                }
        }
    }

    private fun getTimesForStation(entity: ScheduleEntity, stationId: Int): Pair<String?, String?> {
        val stop = entity.stops.find { it.station.id.value == stationId }
        return stop?.arrivalTime?.toString() to stop?.departureTime?.toString()
    }
}
