package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
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
    suspend fun create(userId: Uuid, request: SpotRequest, imageUrl: String, spotId: Uuid): Uuid {
        return dbTransaction {
            val user = UserEntity.findById(userId) ?: throw IllegalArgumentException("User not found")
            val trainModelDto = request.trainModel!!

            val trainModel = TrainModelEntity.find {
                (TrainModelsTable.number eq trainModelDto.number) and 
                (TrainModelsTable.carrierCode eq trainModelDto.carrierCode) and 
                (TrainModelsTable.model eq trainModelDto.model)
            }.firstOrNull() ?: run {
                val carrier = CarrierEntity.findById(trainModelDto.carrierCode) ?: throw IllegalArgumentException("Carrier not found")
                TrainModelEntity.new {
                    this.model = trainModelDto.model
                    this.number = trainModelDto.number
                    this.carrier = carrier
                }
            }

            val station = request.stationId?.let { StationEntity.findById(it) }
            val trainRun = ScheduleEntity.findById(request.trainRunId!!) ?: throw IllegalArgumentException("Train run not found")

            SpotEntity.new(spotId) {
                this.user = user
                this.model = trainModel
                this.station = station
                this.trainRun = trainRun
                this.imageUrl = imageUrl
                this.description = request.description
                this.lat = request.lat
                this.lon = request.lon
            }.id.value
        }
    }

    suspend fun update(id: Uuid, request: SpotRequest, imageUrl: String): Boolean? {
        return dbTransaction {
            val spot = SpotEntity.findById(id) ?: return@dbTransaction null

            val trainModelDto = request.trainModel!!
            val trainModel = TrainModelEntity.find {
                (TrainModelsTable.number eq trainModelDto.number) and 
                (TrainModelsTable.carrierCode eq trainModelDto.carrierCode) and 
                (TrainModelsTable.model eq trainModelDto.model)
            }.firstOrNull() ?: run {
                val carrier = CarrierEntity.findById(trainModelDto.carrierCode) ?: throw IllegalArgumentException("Carrier not found")
                TrainModelEntity.new {
                    this.model = trainModelDto.model
                    this.number = trainModelDto.number
                    this.carrier = carrier
                }
            }

            spot.apply {
                this.model = trainModel
                request.stationId?.let { this.station = StationEntity.findById(it) }
                request.trainRunId?.let { this.trainRun = ScheduleEntity.findById(it)!! }
                this.imageUrl = imageUrl
                request.description?.let { this.description = it }
                request.lat?.let { this.lat = it }
                request.lon?.let { this.lon = it }
            }
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
            val total = SpotEntity.count()
            val offset = (page * limit).toLong()

            val spots = SpotEntity.all()
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
            val total = SpotEntity.find { SpotsTable.userId eq userId }.count()
            val offset = (page * limit).toLong()

            val spots = SpotEntity.find { SpotsTable.userId eq userId }
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