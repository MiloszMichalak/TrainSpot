package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import pl.meleko.trainspot.database.CarriersTable
import pl.meleko.trainspot.database.LikesTable
import pl.meleko.trainspot.database.ScheduleTable
import pl.meleko.trainspot.database.SpotsTable
import pl.meleko.trainspot.database.StationsTable
import pl.meleko.trainspot.database.TrainModelsTable
import pl.meleko.trainspot.database.UsersTable
import pl.meleko.trainspot.database.model.toUser
import pl.meleko.trainspot.database.model.toUserDto
import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.network.dto.ScheduleRouteDto
import pl.meleko.trainspot.network.dto.StationDto
import pl.meleko.trainspot.network.dto.toScheduleRoute
import pl.meleko.trainspot.network.dto.toStation
import pl.meleko.trainspot.requests.SpotRequest
import pl.meleko.trainspot.response.PaginationResponse
import pl.meleko.trainspot.util.dbTransaction
import kotlin.math.ceil
import kotlin.time.toKotlinInstant
import kotlin.uuid.Uuid

object SpotRepository {
    suspend fun create(userId: Uuid, request: SpotRequest, spotId: Uuid): Uuid? = dbTransaction {
        val trainModelDto = request.trainModel ?: return@dbTransaction null

        if (!UsersTable
            .selectAll()
            .where { UsersTable.id eq userId }
            .any()) return@dbTransaction null

        val modelId = findOrCreateModelId(trainModelDto.model, trainModelDto.number, trainModelDto.carrierCode) ?: return@dbTransaction null
        val trainRunId = request.trainRunId ?: return@dbTransaction null

        val run = ScheduleTable.selectAll().where {
            (ScheduleTable.scheduleId eq trainRunId) or (ScheduleTable.id eq trainRunId)
        }.firstOrNull() ?: return@dbTransaction null

        if (request.stationId != null && !StationsTable.selectAll().where { StationsTable.id eq request.stationId }.any()) return@dbTransaction null

        SpotsTable.insert {
            it[SpotsTable.id] = spotId
            it[SpotsTable.userId] = userId
            it[SpotsTable.modelId] = modelId
            it[SpotsTable.stationId] = request.stationId
            it[SpotsTable.trainRunId] = run[ScheduleTable.trainOrderId]
            it[SpotsTable.imageUrl] = ""
            it[SpotsTable.description] = request.description
            it[SpotsTable.lat] = request.lat
            it[SpotsTable.lon] = request.lon
        }
        spotId
    }

    suspend fun update(id: Uuid, request: SpotRequest): Boolean? = dbTransaction {
        if (!SpotsTable.selectAll().where { SpotsTable.id eq id }.any()) return@dbTransaction null

        val trainModelDto = request.trainModel ?: return@dbTransaction null
        val modelId = findOrCreateModelId(trainModelDto.model, trainModelDto.number, trainModelDto.carrierCode) ?: return@dbTransaction null

        val runId = request.trainRunId?.let { value ->
            ScheduleTable
                .selectAll()
                .where { (ScheduleTable.scheduleId eq value) or (ScheduleTable.id eq value) }
                .firstOrNull()
                ?.get(ScheduleTable.trainOrderId)
                ?: return@dbTransaction null
        }

        if (request.stationId != null && !StationsTable.selectAll().where { StationsTable.id eq request.stationId }.any()) return@dbTransaction null

        SpotsTable.update({ SpotsTable.id eq id }) {
            it[SpotsTable.modelId] = modelId
            request.stationId?.let { station -> it[stationId] = station }
            runId?.let { run -> it[trainRunId] = run }
            request.description?.let { value -> it[SpotsTable.description] = value }
            request.lat?.let { value -> it[SpotsTable.lat] = value }
            request.lon?.let { value -> it[SpotsTable.lon] = value }
        } > 0
    }

    suspend fun updateImage(id: Uuid, imageUrl: String): Boolean? = dbTransaction {
        if (!SpotsTable.selectAll()
            .where { SpotsTable.id eq id }.any()) return@dbTransaction null

        SpotsTable
            .update({ SpotsTable.id eq id }) { it[SpotsTable.imageUrl] = imageUrl } > 0
    }

    suspend fun delete(id: Uuid): Boolean = dbTransaction {
        SpotsTable.deleteWhere { SpotsTable.id eq id } > 0
    }

    suspend fun findById(id: Uuid, viewerId: Uuid? = null): Spot? = dbTransaction { loadSpots(listOf(id), viewerId).singleOrNull() }

    suspend fun findAll(page: Int = 0, limit: Int = 20, viewerId: Uuid? = null): PaginationResponse<Spot> = dbTransaction {
        val condition = SpotsTable.imageUrl neq ""

        val total = SpotsTable
            .selectAll()
            .where { condition }
            .count()

        val rows = SpotsTable
            .selectAll()
            .where { condition }
            .orderBy(SpotsTable.spottedAt to SortOrder.DESC)
            .limit(limit)
            .offset((page * limit).toLong())
            .toList()

        val spots = loadSpots(rows.map { it[SpotsTable.id].value }, viewerId)

        PaginationResponse(
            spots,
            total.toInt(),
            page,
            limit,
            ceil(total.toDouble() / limit.coerceAtLeast(1)).toInt().coerceAtLeast(1)
        )
    }

    suspend fun findByUser(userId: Uuid, page: Int = 0, limit: Int = 20, viewerId: Uuid? = null): PaginationResponse<Spot> = dbTransaction {
        val condition = (SpotsTable.userId eq userId) and (SpotsTable.imageUrl neq "")

        val total = SpotsTable
            .selectAll()
            .where { condition }
            .count()

        val rows = SpotsTable
            .selectAll()
            .where { condition }
            .orderBy(SpotsTable.spottedAt to SortOrder.DESC)
            .limit(limit)
            .offset((page * limit).toLong()).toList()

        val spots = loadSpots(rows.map { it[SpotsTable.id].value }, viewerId)

        PaginationResponse(
            spots,
            total.toInt(),
            page,
            limit,
            ceil(total.toDouble() / limit.coerceAtLeast(1)).toInt().coerceAtLeast(1)
        )
    }

    suspend fun findByIds(ids: List<Uuid>, viewerId: Uuid? = null): List<Spot> = dbTransaction {
        loadSpots(ids, viewerId)
    }

    private fun loadSpots(ids: List<Uuid>, viewerId: Uuid?): List<Spot> {
        if (ids.isEmpty()) return emptyList()

        val rows = SpotsTable
            .selectAll()
            .where { SpotsTable.id inList ids }
            .toList()

        val userIds = rows.map { it[SpotsTable.userId].value }.distinct()
        val modelIds = rows.map { it[SpotsTable.modelId].value }.distinct()
        val runIds = rows.map { it[SpotsTable.trainRunId].value }.distinct()

        val users = UsersTable
            .selectAll()
            .where { UsersTable.id inList userIds }
            .associateBy { it[UsersTable.id].value }

        val models = TrainModelsTable
            .selectAll()
            .where { TrainModelsTable.id inList modelIds }
            .associateBy { it[TrainModelsTable.id].value }

        val runs = ScheduleTable
            .selectAll()
            .where { ScheduleTable.id inList runIds }
            .associateBy { it[ScheduleTable.trainOrderId] }

        val carrierCodes = (models.values.map { it[TrainModelsTable.carrierCode].value } + runs.values.map { it[ScheduleTable.carrierCode].value }).distinct()

        val carriers = CarriersTable
            .selectAll()
            .where { CarriersTable.id inList carrierCodes }
            .associateBy { it[CarriersTable.code] }

        val stationIds = (rows.mapNotNull { it[SpotsTable.stationId]?.value } + runs.values.flatMap {
            listOf(it[ScheduleTable.originStationId].value, it[ScheduleTable.destStationId].value)
        }).distinct()

        val stations = StationsTable
            .selectAll()
            .where { StationsTable.id inList stationIds }
            .associateBy { it[StationsTable.id].value }

        val likesBySpot = LikesTable
            .selectAll()
            .where { LikesTable.spotId inList ids }
            .groupBy { it[LikesTable.spotId].value }

        val spotsById = rows.mapNotNull { row ->
            val spotId = row[SpotsTable.id].value
            val userRow = users[row[SpotsTable.userId].value] ?: return@mapNotNull null
            val modelRow = models[row[SpotsTable.modelId].value] ?: return@mapNotNull null
            val runRow = runs[row[SpotsTable.trainRunId].value] ?: return@mapNotNull null
            val carrierRow = carriers[runRow[ScheduleTable.carrierCode].value] ?: return@mapNotNull null
            val originRow = stations[runRow[ScheduleTable.originStationId].value] ?: return@mapNotNull null
            val destRow = stations[runRow[ScheduleTable.destStationId].value] ?: return@mapNotNull null

            val stationDto = row[SpotsTable.stationId]?.value?.let { stationId ->
                stations[stationId]?.let {
                    StationDto(it[StationsTable.id].value, it[StationsTable.name])
                }
            }

            val modelCarrierCode = modelRow[TrainModelsTable.carrierCode].value
            val route = ScheduleRouteDto(
                scheduleId = runRow[ScheduleTable.scheduleId], orderId = runRow[ScheduleTable.trainOrderId].toLong(),
                trainOrderId = runRow[ScheduleTable.trainOrderId], name = runRow[ScheduleTable.trainName],
                carrierCode = carrierRow[CarriersTable.code], nationalNumber = runRow[ScheduleTable.trainNumber],
                commercialCategorySymbol = runRow[ScheduleTable.catSymbol],
                originStation = StationDto(originRow[StationsTable.id].value, originRow[StationsTable.name]),
                destStation = StationDto(destRow[StationsTable.id].value, destRow[StationsTable.name])
            )

            spotId to Spot(
                id = spotId, user = userRow.toUserDto().toUser(),
                model = pl.meleko.trainspot.model.TrainModel(modelRow[TrainModelsTable.model], modelRow[TrainModelsTable.number], modelCarrierCode),
                station = stationDto?.toStation(), trainRun = route.toScheduleRoute(), imageUrl = row[SpotsTable.imageUrl],
                description = row[SpotsTable.description].orEmpty(), lat = row[SpotsTable.lat], lon = row[SpotsTable.lon],
                spottedAt = row[SpotsTable.spottedAt].toInstant().toKotlinInstant(), createdAt = row[SpotsTable.createdAt],
                likes = likesBySpot[spotId]?.size?.toLong() ?: 0,
                isLiked = viewerId != null && likesBySpot[spotId].orEmpty().any { it[LikesTable.userId].value == viewerId }
            )
        }.toMap()

        return ids.mapNotNull(spotsById::get)
    }

    private fun findOrCreateModelId(model: String, number: String, carrierCode: String): Uuid? {
        val condition = (TrainModelsTable.number eq number) and
            (TrainModelsTable.carrierCode eq carrierCode) and
            (TrainModelsTable.model eq model)

        TrainModelsTable.selectAll().where { condition }.firstOrNull()?.let {
            return it[TrainModelsTable.id].value
        }

        if (!CarriersTable.selectAll().where { CarriersTable.id eq carrierCode }.any()) return null

        return TrainModelsTable.insertAndGetId {
            it[TrainModelsTable.model] = model
            it[TrainModelsTable.number] = number
            it[TrainModelsTable.carrierCode] = carrierCode
        }.value
    }
}
