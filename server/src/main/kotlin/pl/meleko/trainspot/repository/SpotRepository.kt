package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.core.neq
import pl.meleko.trainspot.database.ScheduleTable
import pl.meleko.trainspot.database.SpotsTable
import pl.meleko.trainspot.database.TrainModelsTable
import pl.meleko.trainspot.database.entities.CarrierEntity
import pl.meleko.trainspot.database.entities.ScheduleEntity
import pl.meleko.trainspot.database.entities.SpotEntity
import pl.meleko.trainspot.database.entities.StationEntity
import pl.meleko.trainspot.database.entities.TrainModelEntity
import pl.meleko.trainspot.database.entities.UserEntity
import pl.meleko.trainspot.database.model.SpotDto
import pl.meleko.trainspot.database.model.toSpotResponse
import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.requests.SpotRequest
import pl.meleko.trainspot.response.PaginationResponse
import pl.meleko.trainspot.util.dbTransaction
import kotlin.math.ceil
import kotlin.uuid.Uuid

object SpotRepository {
    suspend fun create(userId: Uuid, request: SpotRequest, spotId: Uuid): Uuid? {
        return dbTransaction {
            val user = UserEntity.findById(userId) ?: return@dbTransaction null
            val trainModelDto = request.trainModel ?: return@dbTransaction null

            val trainModel = TrainModelEntity.find {
                (TrainModelsTable.number eq trainModelDto.number) and 
                (TrainModelsTable.carrierCode eq trainModelDto.carrierCode) and 
                (TrainModelsTable.model eq trainModelDto.model)
            }.firstOrNull() ?: run {
                val carrier = CarrierEntity.findById(trainModelDto.carrierCode) ?: return@dbTransaction null
                TrainModelEntity.new {
                    this.model = trainModelDto.model
                    this.number = trainModelDto.number
                    this.carrier = carrier
                }
            }

            val station = request.stationId?.let { StationEntity.findById(it) }
            val trainRunId = request.trainRunId ?: return@dbTransaction null
            val trainRun = ScheduleEntity.find {
                (ScheduleTable.scheduleId eq trainRunId) or (ScheduleTable.id eq trainRunId)
            }.firstOrNull() ?: return@dbTransaction null

            SpotEntity.new(spotId) {
                this.user = user
                this.model = trainModel
                this.station = station
                this.trainRun = trainRun
                this.imageUrl = ""
                this.description = request.description
                this.lat = request.lat
                this.lon = request.lon
            }.id.value
        }
    }

    suspend fun update(id: Uuid, request: SpotRequest): Boolean? {
        return dbTransaction {
            val spot = SpotEntity.findById(id) ?: return@dbTransaction null

            val trainModelDto = request.trainModel!!
            val trainModel = TrainModelEntity.find {
                (TrainModelsTable.number eq trainModelDto.number) and 
                (TrainModelsTable.carrierCode eq trainModelDto.carrierCode) and 
                (TrainModelsTable.model eq trainModelDto.model)
            }.firstOrNull() ?: run {
                val carrier = CarrierEntity.findById(trainModelDto.carrierCode) ?: return@dbTransaction null
                TrainModelEntity.new {
                    this.model = trainModelDto.model
                    this.number = trainModelDto.number
                    this.carrier = carrier
                }
            }

            spot.apply {
                this.model = trainModel
                request.stationId?.let { this.station = StationEntity.findById(it) }
                request.trainRunId?.let { runId ->
                    this.trainRun = ScheduleEntity.find {
                        (ScheduleTable.scheduleId eq runId) or (ScheduleTable.id eq runId)
                    }.firstOrNull() ?: return@dbTransaction null
                }
                request.description?.let { this.description = it }
                request.lat?.let { this.lat = it }
                request.lon?.let { this.lon = it }
            }
            true
        }
    }

    suspend fun updateImage(id: Uuid, imageUrl: String): Boolean? {
        return dbTransaction {
            val spot = SpotEntity.findById(id) ?: return@dbTransaction null
            spot.imageUrl = imageUrl
            true
        }
    }

    suspend fun delete(id: Uuid): Boolean {
        return dbTransaction {
            SpotEntity.findById(id)?.delete()
            true
        }
    }

    suspend fun findById(id: Uuid): SpotDto? {
        return dbTransaction {
            SpotEntity.findById(id)?.toDto()
        }
    }

    suspend fun findAll(page: Int = 0, limit: Int = 20): PaginationResponse<Spot> {
        return dbTransaction {
            val visibleSpots = SpotEntity.find { SpotsTable.imageUrl neq "" }
            val total = visibleSpots.count()
            val offset = (page * limit).toLong()

            val spots = SpotEntity.find { SpotsTable.imageUrl neq "" }
                .orderBy(SpotsTable.spottedAt to SortOrder.DESC)
                .limit(limit)
                .offset(offset)
                .map { it.toDto().toSpotResponse() }

            PaginationResponse(
                items = spots,
                total = total.toInt(),
                page = page,
                limit = limit,
                totalPages = ceil(total.toDouble() / limit.coerceAtLeast(1)).toInt().coerceAtLeast(1)
            )
        }
    }

    suspend fun findByUser(userId: Uuid, page: Int = 0, limit: Int = 20): PaginationResponse<Spot> {
        return dbTransaction {
            val total = SpotEntity.find { (SpotsTable.userId eq userId) and (SpotsTable.imageUrl neq "") }.count()
            val offset = (page * limit).toLong()

            val spots = SpotEntity.find { (SpotsTable.userId eq userId) and (SpotsTable.imageUrl neq "") }
                .orderBy(SpotsTable.spottedAt to SortOrder.DESC)
                .limit(limit)
                .offset(offset)
                .map { it.toDto().toSpotResponse() }

            PaginationResponse(
                items = spots,
                total = total.toInt(),
                page = page,
                limit = limit,
                totalPages = ceil(total.toDouble() / limit.coerceAtLeast(1)).toInt().coerceAtLeast(1)
            )
        }
    }

    suspend fun findByIds(ids: List<Uuid>): List<Spot> {
        return dbTransaction {
            SpotEntity.find { SpotsTable.id inList ids }
                .map { it.toDto().toSpotResponse() }
        }
    }
}
