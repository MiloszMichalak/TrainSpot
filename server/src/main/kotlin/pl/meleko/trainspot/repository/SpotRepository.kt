package pl.meleko.trainspot.repository

import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalTime
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.update
import pl.meleko.trainspot.database.CommentsTable
import pl.meleko.trainspot.database.LikesTable
import pl.meleko.trainspot.database.ScheduleTable
import pl.meleko.trainspot.database.SpotsTable
import pl.meleko.trainspot.database.StationsTable
import pl.meleko.trainspot.database.TrainModelsTable
import pl.meleko.trainspot.database.TrainStopsTable
import pl.meleko.trainspot.database.TrainTypesTable
import pl.meleko.trainspot.database.TrainVehiclesTable
import pl.meleko.trainspot.database.UsersTable
import pl.meleko.trainspot.database.model.toUser
import pl.meleko.trainspot.database.model.toUserDto
import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.model.TrainModel
import pl.meleko.trainspot.network.dto.ScheduleRouteDto
import pl.meleko.trainspot.network.dto.StationDto
import pl.meleko.trainspot.network.dto.toScheduleRoute
import pl.meleko.trainspot.network.dto.toStation
import pl.meleko.trainspot.requests.SpotRequest
import pl.meleko.trainspot.requests.hasValidTrainSelection
import pl.meleko.trainspot.response.PaginationResponse
import pl.meleko.trainspot.util.dbTransaction
import java.time.OffsetDateTime
import java.time.ZoneId
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

        if (!validReferences(request)) return@dbTransaction null

        val modelId = TrainModelRepository.findOrCreateModelId(trainModelDto) ?: return@dbTransaction null
        val spottedAt = OffsetDateTime.now(ZoneId.of("Europe/Warsaw"))
        val trainRunId = resolveTrainRun(request, spottedAt)

        SpotsTable.insert {
            it[SpotsTable.id] = spotId
            it[SpotsTable.userId] = userId
            it[SpotsTable.modelId] = modelId
            it[SpotsTable.stationId] = request.stationId
            it[SpotsTable.trainRunId] = trainRunId
            it[SpotsTable.imageUrl] = ""
            it[SpotsTable.description] = request.description
            it[SpotsTable.lat] = request.lat
            it[SpotsTable.lon] = request.lon
            it[SpotsTable.spottedAt] = spottedAt
        }
        spotId
    }

    suspend fun update(id: Uuid, request: SpotRequest): Boolean? = dbTransaction {
        if (!SpotsTable.selectAll().where { SpotsTable.id eq id }.any()) return@dbTransaction null

        val trainModelDto = request.trainModel ?: return@dbTransaction null

        if (!validReferences(request)) return@dbTransaction null

        val modelId = TrainModelRepository.findOrCreateModelId(trainModelDto) ?: return@dbTransaction null
        val spottedAt = SpotsTable.selectAll().where { SpotsTable.id eq id }.single()[SpotsTable.spottedAt]
        val existingRunId = SpotsTable.selectAll().where { SpotsTable.id eq id }.single()[SpotsTable.trainRunId]?.value
        val runId = resolveTrainRun(request, spottedAt, existingRunId)

        SpotsTable.update({ SpotsTable.id eq id }) {
            it[SpotsTable.modelId] = modelId
            request.stationId?.let { station -> it[stationId] = station }
            it[trainRunId] = runId
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

    suspend fun findById(id: Uuid, viewerId: Uuid? = null): Spot? = dbTransaction {
        loadSpots(listOf(id), viewerId).singleOrNull()
    }

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
        val runIds = rows.mapNotNull { it[SpotsTable.trainRunId]?.value }.distinct()

        val users = UsersTable
            .selectAll()
            .where { UsersTable.id inList userIds }
            .associateBy { it[UsersTable.id].value }

        val models = TrainModelsTable
            .selectAll()
            .where { TrainModelsTable.id inList modelIds }
            .associateBy { it[TrainModelsTable.id].value }

        val vehicles = TrainVehiclesTable.selectAll()
            .where {
                TrainVehiclesTable.id inList models.values.map { it[TrainModelsTable.vehicle].value }
                    .distinct()
            }
            .associateBy { it[TrainVehiclesTable.id].value }

        val types = TrainTypesTable.selectAll()
            .where {
                TrainTypesTable.id inList vehicles.values.map { it[TrainVehiclesTable.typeId].value }
                    .distinct()
            }
            .associateBy { it[TrainTypesTable.id].value }

        val runs = ScheduleTable
            .selectAll()
            .where { ScheduleTable.id inList runIds }
            .associateBy { it[ScheduleTable.trainOrderId] }

        val stationIds =
            (rows.mapNotNull { it[SpotsTable.stationId]?.value } + runs.values.flatMap {
                listOfNotNull(
                    it[ScheduleTable.originStationId]?.value,
                    it[ScheduleTable.destStationId]?.value
                )
            }).distinct()

        val stations = StationsTable
            .selectAll()
            .where { StationsTable.id inList stationIds }
            .associateBy { it[StationsTable.id].value }

        val likeRows = LikesTable
            .selectAll()
            .where { LikesTable.spotId inList ids }
            .toList()
        val likes = likeRows.groupBy { it[LikesTable.spotId].value }
        val likedSpotIds = viewerId?.let { currentUserId ->
            likeRows.asSequence()
                .filter { it[LikesTable.userId].value == currentUserId }
                .map { it[LikesTable.spotId].value }
                .toSet()
        }.orEmpty()
        val commentsCounts = CommentsTable
            .selectAll()
            .where { CommentsTable.spotId inList ids }
            .groupBy { it[CommentsTable.spotId].value }
            .mapValues { it.value.size.toLong() }

        val spotsById = rows.mapNotNull { row ->
            val spotId = row[SpotsTable.id].value
            val userRow = users[row[SpotsTable.userId].value] ?: return@mapNotNull null
            val modelRow = models[row[SpotsTable.modelId].value] ?: return@mapNotNull null
            val vehicleRow =
                vehicles[modelRow[TrainModelsTable.vehicle].value] ?: return@mapNotNull null
            val typeRow =
                types[vehicleRow[TrainVehiclesTable.typeId].value] ?: return@mapNotNull null
            val runRow = row[SpotsTable.trainRunId]?.value?.let(runs::get)
            val originRow = runRow?.get(ScheduleTable.originStationId)?.value?.let(stations::get)
            val destRow = runRow?.get(ScheduleTable.destStationId)?.value?.let(stations::get)

            val stationDto = row[SpotsTable.stationId]?.value?.let { stationId ->
                stations[stationId]?.let {
                    StationDto(
                        id = it[StationsTable.id].value,
                        name = it[StationsTable.name],
                        latitude = it[StationsTable.latitude],
                        longitude = it[StationsTable.longitude]
                    )
                }
            }

            val modelCarrierCode = modelRow[TrainModelsTable.carrierCode]?.value
            val route = runRow?.let { ScheduleRouteDto(
                scheduleId = runRow[ScheduleTable.scheduleId],
                orderId = runRow[ScheduleTable.trainOrderId].toLong(),
                trainOrderId = runRow[ScheduleTable.trainOrderId],
                name = runRow[ScheduleTable.trainName],
                carrierCode = runRow[ScheduleTable.carrierCode]?.value.orEmpty(),
                nationalNumber = runRow[ScheduleTable.trainNumber],
                commercialCategorySymbol = runRow[ScheduleTable.catSymbol],
                originStation = originRow?.let { StationDto(
                    id = it[StationsTable.id].value,
                    name = it[StationsTable.name],
                    latitude = it[StationsTable.latitude],
                    longitude = it[StationsTable.longitude]
                ) },
                destStation = destRow?.let { StationDto(
                    id = it[StationsTable.id].value,
                    name = it[StationsTable.name],
                    latitude = it[StationsTable.latitude],
                    longitude = it[StationsTable.longitude]
                ) }
            ) }

            spotId to Spot(
                id = spotId,
                user = userRow.toUserDto().toUser(),
                model = TrainModel(
                    model = listOf(
                        typeRow[TrainTypesTable.name],
                        vehicleRow[TrainVehiclesTable.model]
                    )
                        .filter { it.isNotEmpty() }.joinToString("-"),
                    number = modelRow[TrainModelsTable.number],
                    carrierCode = modelCarrierCode
                ),
                station = stationDto?.toStation(),
                trainRun = route?.toScheduleRoute(),
                imageUrl = row[SpotsTable.imageUrl],
                description = row[SpotsTable.description].orEmpty(),
                lat = row[SpotsTable.lat],
                lon = row[SpotsTable.lon],
                spottedAt = row[SpotsTable.spottedAt].toInstant().toKotlinInstant(),
                createdAt = row[SpotsTable.createdAt],
                likes = likes[row[SpotsTable.id].value]?.size?.toLong() ?: 0,
                isLiked = row[SpotsTable.id].value in likedSpotIds,
                commentsCount = commentsCounts[row[SpotsTable.id].value] ?: 0
            )
        }.toMap()

        return ids.mapNotNull(spotsById::get)
    }

    private fun validReferences(request: SpotRequest): Boolean {
        if (!request.hasValidTrainSelection()) return false
        val stationIds = listOfNotNull(request.stationId, request.originStationId, request.destinationStationId).distinct()
        return !(stationIds.isNotEmpty()
                && StationsTable.selectAll().where { StationsTable.id inList stationIds }.count() != stationIds.size.toLong())
                && (request.trainRunId == null || ScheduleTable.selectAll().where { ScheduleTable.id eq request.trainRunId }.any())
    }

    private fun resolveTrainRun(request: SpotRequest, spottedAt: OffsetDateTime, existingRunId: Int? = null): Int? {
        val number = request.manualTrainNumber?.trim()?.takeIf(String::isNotEmpty) ?: return request.trainRunId

        // Negative IDs are reserved for community runs, independently of imported PKP identifiers.
        val reusableRunId = existingRunId?.takeIf { id ->
            id < 0 && SpotsTable.selectAll().where { SpotsTable.trainRunId eq id }.count() == 1L
        }

        val runId = reusableRunId ?: TransactionManager.current().exec("SELECT nextval('manual_train_run_id_seq')") { result ->
            check(result.next())
            result.getInt(1)
        } ?: error("Unable to allocate manual train run ID")

        val localTime = spottedAt.atZoneSameInstant(ZoneId.of("Europe/Warsaw"))
        val carrier = request.trainModel?.carrierCode?.trim()?.takeIf { it.isNotEmpty() }

        if (existingRunId == runId) {
            ScheduleTable.update({ ScheduleTable.id eq runId }) {
                it[trainNumber] = number
                it[carrierCode] = carrier
                it[originStationId] = request.originStationId
                it[destStationId] = request.destinationStationId
            }
            TrainStopsTable.deleteWhere { trainRunId eq runId }
        } else {
            ScheduleTable.insert {
                it[trainOrderId] = runId
                it[scheduleId] = runId
                it[trainNumber] = number
                it[carrierCode] = carrier
                it[catSymbol] = ""
                it[operatingDate] = localTime.toLocalDate().toKotlinLocalDate()
                it[originStationId] = request.originStationId
                it[destStationId] = request.destinationStationId
            }
        }
        TrainStopsTable.insert {
            it[trainRunId] = runId
            it[stationId] = requireNotNull(request.stationId)
            it[orderNumber] = 1
            it[arrTrainNum] = number
            it[depTrainNum] = number
            it[arrivalTime] = localTime.toLocalTime().toKotlinLocalTime()
            it[departureTime] = localTime.toLocalTime().toKotlinLocalTime()
            it[arrDayOffset] = 0
            it[depDayOffset] = 0
        }
        return runId
    }
}
