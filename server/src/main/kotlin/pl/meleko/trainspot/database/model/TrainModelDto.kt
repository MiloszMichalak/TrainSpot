package pl.meleko.trainspot.database.model

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