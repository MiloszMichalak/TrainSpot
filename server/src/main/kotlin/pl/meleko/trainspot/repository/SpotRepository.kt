package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import pl.meleko.trainspot.PaginationResponse
import pl.meleko.trainspot.SpotResponse
import pl.meleko.trainspot.database.SpotsTable
import pl.meleko.trainspot.database.model.SpotDto
import pl.meleko.trainspot.database.model.toSpotDto
import pl.meleko.trainspot.database.model.toSpotResponse
import pl.meleko.trainspot.requests.SpotRequest
import kotlin.math.ceil
import kotlin.uuid.Uuid

object SpotRepository {
    fun create(userId: Uuid, request: SpotRequest, imageUrl: String): Uuid = transaction {
        SpotsTable.insert {
            it[SpotsTable.userId] = userId
            it[SpotsTable.modelId] = request.modelId
            it[SpotsTable.stationId] = request.stationId
            it[SpotsTable.trainRunId] = request.trainRunId
            it[SpotsTable.imageUrl] = imageUrl
            it[SpotsTable.description] = request.description
            it[SpotsTable.lat] = request.lat
            it[SpotsTable.lon] = request.lon
        }[SpotsTable.id].value
    }

    fun update(id: Uuid, request: SpotRequest, imageUrl: String): Boolean? = transaction {
        val spot = SpotsTable.selectAll()
            .where { SpotsTable.id eq id }
            .firstOrNull()

        if (spot == null) return@transaction null

        SpotsTable.update({ SpotsTable.id eq id }) { row ->
            request.modelId?.let { row[SpotsTable.modelId] = it }
            request.stationId?.let { row[SpotsTable.stationId] = it }
            request.trainRunId?.let { row[SpotsTable.trainRunId] = it }
            row[SpotsTable.imageUrl] = imageUrl
            request.description?.let { row[SpotsTable.description] = it }
            request.lat?.let { row[SpotsTable.lat] = it }
            request.lon?.let { row[SpotsTable.lon] = it }
        } > 0
    }

    fun delete(id: Uuid): Boolean = transaction {
        SpotsTable.deleteWhere { SpotsTable.id eq id } > 0
    }

    fun findById(id: Uuid): SpotDto? = transaction {
        SpotsTable.selectAll()
            .where { SpotsTable.id eq id }
            .firstOrNull()
            ?.toSpotDto()
    }

    fun findAll(page: Int = 0, limit: Int = 20): PaginationResponse<SpotDto> {
        return transaction {
            val total = SpotsTable.selectAll().count()

            val offset = (page * limit).toLong()

            val spots = SpotsTable
                .selectAll()
                .orderBy(SpotsTable.spottedAt to SortOrder.DESC)
                .offset(offset)
                .limit(limit)
                .map { row -> row.toSpotDto() }

            PaginationResponse(
                items = spots,
                total = total.toInt(),
                page = page,
                limit = limit,
                totalPages = ceil(total.toDouble() / limit.coerceAtLeast(1)).toInt().coerceAtLeast(1)
            )
        }
    }

    fun findByUser(userId: Uuid, page: Int = 0, limit: Int = 20): PaginationResponse<SpotResponse> {
        return transaction {
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
                .map { row -> row.toSpotDto().toSpotResponse(LikesRepository.likesCountFor(row[SpotsTable.id].value)) }

            PaginationResponse(
                items = spots,
                total = total.toInt(),
                page = page,
                limit = limit,
                totalPages = ceil(total.toDouble() / limit.coerceAtLeast(1)).toInt().coerceAtLeast(1)
            )
        }
    }
}


