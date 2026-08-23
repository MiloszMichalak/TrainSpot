package pl.meleko.trainspot.database.entities

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import pl.meleko.trainspot.database.TrainModelsTable
import pl.meleko.trainspot.database.model.TrainModelDto
import kotlin.uuid.Uuid

class TrainModelEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    companion object : UuidEntityClass<TrainModelEntity>(TrainModelsTable)

    var model by TrainModelsTable.model
    var number by TrainModelsTable.number
    var carrier by CarrierEntity referencedOn TrainModelsTable.carrierCode
    var createdAt by TrainModelsTable.createdAt

    fun toDto() = TrainModelDto(
        id = id.value,
        model = model,
        number = number,
        carrierCode = carrier.id.value,
        createdAt = createdAt
    )
}
