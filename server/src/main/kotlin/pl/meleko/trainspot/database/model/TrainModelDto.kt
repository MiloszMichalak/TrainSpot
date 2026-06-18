package pl.meleko.trainspot.database.model

import org.jetbrains.exposed.v1.core.ResultRow
import pl.meleko.trainspot.database.TrainModelsTable
import pl.meleko.trainspot.model.TrainModel
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class TrainModelDto(
    val id: Uuid,
    val model: String,
    val number: String,
    val carrierCode: String,
    val createdAt: Instant,
)

fun TrainModelDto.toTrainModel() = TrainModel(
    model = this.model,
    number = this.number,
    carrierCode = this.carrierCode
)

fun ResultRow.toTrainModelDto() = TrainModelDto(
    id = this[TrainModelsTable.id].value,
    model = this[TrainModelsTable.model],
    number = this[TrainModelsTable.number],
    carrierCode = this[TrainModelsTable.carrierCode],
    createdAt = this[TrainModelsTable.createdAt],
)