package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import pl.meleko.trainspot.database.SpotsTable
import pl.meleko.trainspot.database.model.SpotDto
import pl.meleko.trainspot.database.model.toSpotDto
import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.requests.SpotRequest
import pl.meleko.trainspot.response.PaginationResponse
import pl.meleko.trainspot.util.dbTransaction
import kotlin.math.ceil
import kotlin.uuid.Uuid

object SpotRepository {
    suspend fun create(userId: Uuid, request: SpotRequest, imageUrl: String, spotId: Uuid): Uuid {
        return dbTransaction {
            val trainModel = request.trainModel!!
            val trainModelId = TrainModelRepository.createOrInsert(
                trainModel.model,
                trainModel.number,
                trainModel.carrierCode
            )?.id

            SpotsTable.insert {
                it[SpotsTable.id] = spotId
                it[SpotsTable.userId] = userId
                it[SpotsTable.modelId] = trainModelId
                it[SpotsTable.stationId] = request.stationId
                it[SpotsTable.trainRunId] = request.trainRunId
                it[SpotsTable.imageUrl] = imageUrl
                it[SpotsTable.description] = request.description
                it[SpotsTable.lat] = request.lat
                it[SpotsTable.lon] = request.lon
            }[SpotsTable.id].value
        }
    }

    suspend fun update(id: Uuid, request: SpotRequest, imageUrl: String): Boolean? {
        return dbTransaction {
            val spot = SpotsTable.selectAll()
                .where { SpotsTable.id eq id }
                .firstOrNull()

            if (spot == null) return@dbTransaction null

            val trainModel = request.trainModel!!
            val trainModelId = TrainModelRepository.createOrInsert(
                trainModel.model,
                trainModel.number,
                trainModel.carrierCode
            )?.id

            SpotsTable.update({ SpotsTable.id eq id }) { row ->
                request.trainModel?.let { row[SpotsTable.modelId] = trainModelId }
                request.stationId?.let { row[SpotsTable.stationId] = it }
                request.trainRunId?.let { row[SpotsTable.trainRunId] = it }
                row[SpotsTable.imageUrl] = imageUrl
                request.description?.let { row[SpotsTable.description] = it }
                request.lat?.let { row[SpotsTable.lat] = it }
                request.lon?.let { row[SpotsTable.lon] = it }
            } > 0
        }
    }

    suspend fun delete(id: Uuid): Boolean {
        return dbTransaction {
            SpotsTable.deleteWhere { SpotsTable.id eq id } > 0
        }
    }

    suspend fun findById(id: Uuid): SpotDto? {
        return dbTransaction {
            SpotsTable.selectAll()
                .where { SpotsTable.id eq id }
                .firstOrNull()
                ?.toSpotDto()
        }
    }

    suspend fun findAll(page: Int = 0, limit: Int = 20): PaginationResponse<Spot> {
        return dbTransaction {
            val total = SpotsTable.selectAll().count()

            val offset = (page * limit)

            val spots = SpotsTable
                .selectAll()
                .orderBy(SpotsTable.spottedAt to SortOrder.DESC)
                .offset(offset.toLong())
                .limit(limit)
                .map { row -> row.toSpotDto().toSpotResponse() }

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
            val total = SpotsTable
                .selectAll()
                .where { SpotsTable.userId eq userId }
                .count()

            val offset = (page * limit).toLong()

            val spots = SpotsTable
                .selectAll()
                .where { SpotsTable.userId eq userId }
                .orderBy(SpotsTable.spottedAt to SortOrder.DESC)
                .offset(offset)
                .limit(limit)
                .map { row -> row.toSpotDto().toSpotResponse() }

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
            SpotsTable.selectAll()
                .where { SpotsTable.id inList ids }
                .map { row -> row.toSpotDto().toSpotResponse() }
        }
    }
}