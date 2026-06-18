package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import pl.meleko.trainspot.database.TrainModelsTable
import pl.meleko.trainspot.database.model.TrainModelDto
import pl.meleko.trainspot.database.model.toTrainModelDto
import pl.meleko.trainspot.util.dbTransaction
import kotlin.uuid.Uuid

object TrainModelRepository {
    suspend fun findById(id: Uuid): TrainModelDto? {
        return dbTransaction {
            TrainModelsTable.selectAll()
                .where { TrainModelsTable.id eq id }
                .firstOrNull()
                ?.toTrainModelDto()
        }
    }

    suspend fun createOrInsert(model: String, number: String, carrierCode: String): TrainModelDto? =
        dbTransaction {
            val existing = TrainModelsTable.selectAll()
                .where {
                    (TrainModelsTable.number eq number) and (TrainModelsTable.carrierCode eq carrierCode) and (TrainModelsTable.model eq model)
                }
                .firstOrNull()
                ?.toTrainModelDto()

            if (existing != null) {
                return@dbTransaction existing
            }

            val id = TrainModelsTable.insert {
                it[TrainModelsTable.model] = model
                it[TrainModelsTable.number] = number
                it[TrainModelsTable.carrierCode] = carrierCode
            }[TrainModelsTable.id].value

            findById(id)
        }
}