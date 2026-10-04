package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import pl.meleko.trainspot.database.CarriersTable
import pl.meleko.trainspot.database.TrainModelsTable
import pl.meleko.trainspot.database.TrainTypesTable
import pl.meleko.trainspot.database.TrainVehiclesTable
import pl.meleko.trainspot.model.TrainModel
import kotlin.uuid.Uuid

object TrainModelRepository {
    fun findOrCreateModelId(dto: TrainModel): Uuid? {
        val parts = dto.model.trim().split('-')
        val type = parts[0].trim()
        val vehicleModel = parts.getOrNull(1)?.trim().orEmpty()
        val number = dto.number.trim()
        val carrierCode = dto.carrierCode.trim()

        if (type.isEmpty() || type.length > 32 || vehicleModel.length > 16 ||
            number.isEmpty() || number.length > 16 || carrierCode.isEmpty() || carrierCode.length > 16
        ) return null

        if (!CarriersTable.selectAll().where { CarriersTable.id eq carrierCode }.any()) return null

        TrainTypesTable.insertIgnore {
            it[TrainTypesTable.name] = type
        }
        val typeId = TrainTypesTable.selectAll().where { TrainTypesTable.name eq type }
            .single()[TrainTypesTable.id].value

        TrainVehiclesTable.insertIgnore {
            it[TrainVehiclesTable.typeId] = typeId
            it[TrainVehiclesTable.model] = vehicleModel
        }

        val vehicleId = TrainVehiclesTable.selectAll().where {
            (TrainVehiclesTable.typeId eq typeId) and (TrainVehiclesTable.model eq vehicleModel)
        }.single()[TrainVehiclesTable.id].value

        TrainModelsTable.insertIgnore {
            it[TrainModelsTable.vehicle] = vehicleId
            it[TrainModelsTable.number] = number
            it[TrainModelsTable.carrierCode] = carrierCode
        }

        return TrainModelsTable.selectAll().where {
            (TrainModelsTable.vehicle eq vehicleId) and (TrainModelsTable.number eq number) and
                    (TrainModelsTable.carrierCode eq carrierCode)
        }.single()[TrainModelsTable.id].value
    }
}