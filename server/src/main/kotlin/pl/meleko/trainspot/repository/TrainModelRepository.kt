package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import pl.meleko.trainspot.database.TrainModelsTable
import pl.meleko.trainspot.database.entities.CarrierEntity
import pl.meleko.trainspot.database.entities.TrainModelEntity
import pl.meleko.trainspot.database.model.TrainModelDto
import pl.meleko.trainspot.util.dbTransaction
import kotlin.uuid.Uuid

object TrainModelRepository {
    suspend fun findById(id: Uuid): TrainModelDto? {
        return dbTransaction {
            TrainModelEntity.findById(id)?.toDto()
        }
    }

    suspend fun createOrInsert(model: String, number: String, carrierCode: String): TrainModelDto =
        dbTransaction {
            val existing = TrainModelEntity.find {
                (TrainModelsTable.number eq number) and 
                (TrainModelsTable.carrierCode eq carrierCode) and 
                (TrainModelsTable.model eq model)
            }.firstOrNull()

            if (existing != null) {
                return@dbTransaction existing.toDto()
            }

            val carrier = CarrierEntity.findById(carrierCode) ?: throw IllegalArgumentException("Carrier $carrierCode not found")

            TrainModelEntity.new {
                this.model = model
                this.number = number
                this.carrier = carrier
            }.toDto()
        }
}