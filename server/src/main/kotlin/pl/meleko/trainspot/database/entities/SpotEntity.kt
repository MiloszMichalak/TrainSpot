package pl.meleko.trainspot.database.entities

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import pl.meleko.trainspot.database.LikesTable
import pl.meleko.trainspot.database.SpotsTable
import pl.meleko.trainspot.database.model.SpotDto
import kotlin.uuid.Uuid

class SpotEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    companion object : UuidEntityClass<SpotEntity>(SpotsTable)

    var user by UserEntity referencedOn SpotsTable.userId
    var model by TrainModelEntity referencedOn SpotsTable.modelId
    var station by StationEntity optionalReferencedOn SpotsTable.stationId
    var trainRun by ScheduleEntity referencedOn SpotsTable.trainRunId
    var imageUrl by SpotsTable.imageUrl
    var description by SpotsTable.description
    var lat by SpotsTable.lat
    var lon by SpotsTable.lon
    var spottedAt by SpotsTable.spottedAt
    var createdAt by SpotsTable.createdAt

    val likes by LikeEntity referrersOn LikesTable.spotId

    fun toDto() = SpotDto(
        id = id.value,
        user = user.toDto(),
        model = model.toDto(),
        station = station?.toDto(),
        trainRun = trainRun.toDto(),
        imageUrl = imageUrl,
        description = description,
        lat = lat,
        lon = lon,
        spottedAt = spottedAt,
        createdAt = createdAt,
        likesCount = likes.count()
    )
}
