package pl.meleko.trainspot.database.entities

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import pl.meleko.trainspot.database.LikesTable
import pl.meleko.trainspot.database.model.LikeDto
import kotlin.uuid.Uuid

class LikeEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    companion object : UuidEntityClass<LikeEntity>(LikesTable)

    var user by UserEntity referencedOn LikesTable.userId
    var spot by SpotEntity referencedOn LikesTable.spotId
    var createdAt by LikesTable.createdAt

    fun toDto() = LikeDto(
        id = id.value,
        userId = user.id.value,
        spotId = spot.id.value,
        createdAt = createdAt
    )
}
