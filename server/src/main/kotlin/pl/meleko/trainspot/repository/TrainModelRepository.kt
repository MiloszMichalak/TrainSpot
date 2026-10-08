package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import pl.meleko.trainspot.database.CarriersTable
import pl.meleko.trainspot.database.TrainModelsTable
import pl.meleko.trainspot.database.TrainTypesTable
import pl.meleko.trainspot.database.TrainVehiclesTable
import pl.meleko.trainspot.model.TrainModel
import kotlin.uuid.Uuid

object TrainModelRepository {
    // Called inside the spot transaction: type, vehicle and model are saved atomically.
    fun findOrCreateModelId(dto: TrainModel): Uuid? {
        val parts = dto.model.trim().split('-', limit = 2)
        val type = parts[0].trim()
        val vehicleModel = parts.getOrNull(1)?.trim().orEmpty()
        val number = dto.number?.trim()?.takeIf(String::isNotEmpty)
        val carrierCode = dto.carrierCode?.trim()?.takeIf(String::isNotEmpty)

        if (type.isEmpty() || type.length > 32 || vehicleModel.length > 16 ||
            (number != null && number.length > 16) || (carrierCode != null && carrierCode.length > 16)
        ) return null

        if (carrierCode != null && !CarriersTable.selectAll().where { CarriersTable.id eq carrierCode }.any()) return null

        TrainTypesTable.insertIgnore {
            it[TrainTypesTable.name] = type
        }
        // Serialize creation within one type, including models with NULL number/carrier.
        val typeId = TrainTypesTable.selectAll()
            .where { TrainTypesTable.name eq type }
            .forUpdate()
            .single()[TrainTypesTable.id].value

        // Earlier versions could create duplicates. Reuse a deterministic existing row
        // without deleting records that may already be referenced by spots.
        val vehicleId = TrainVehiclesTable.selectAll().where {
            (TrainVehiclesTable.typeId eq typeId) and (TrainVehiclesTable.model eq vehicleModel)
        }.orderBy(
            TrainVehiclesTable.isVerified to SortOrder.DESC,
            TrainVehiclesTable.createdAt to SortOrder.ASC,
            TrainVehiclesTable.id to SortOrder.ASC
        ).limit(1).singleOrNull()?.get(TrainVehiclesTable.id)?.value
            ?: TrainVehiclesTable.insertAndGetId {
                it[TrainVehiclesTable.typeId] = typeId
                it[TrainVehiclesTable.model] = vehicleModel
                it[TrainVehiclesTable.isVerified] = false
            }.value

        return TrainModelsTable.selectAll().where {
            (TrainModelsTable.vehicle eq vehicleId) and (TrainModelsTable.number eq number) and
                (TrainModelsTable.carrierCode eq carrierCode)
        }.orderBy(
            TrainModelsTable.createdAt to SortOrder.ASC,
            TrainModelsTable.id to SortOrder.ASC
        ).limit(1).singleOrNull()?.get(TrainModelsTable.id)?.value
            ?: TrainModelsTable.insertAndGetId {
                it[TrainModelsTable.vehicle] = vehicleId
                it[TrainModelsTable.number] = number
                it[TrainModelsTable.carrierCode] = carrierCode
            }.value
    }
}