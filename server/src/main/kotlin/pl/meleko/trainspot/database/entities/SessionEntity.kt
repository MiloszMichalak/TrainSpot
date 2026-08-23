package pl.meleko.trainspot.database.entities

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import pl.meleko.trainspot.database.SessionsTable
import kotlin.uuid.Uuid

class SessionEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    companion object : UuidEntityClass<SessionEntity>(SessionsTable)

    var user by UserEntity referencedOn SessionsTable.userId
    var createdAt by SessionsTable.createdAt
    var lastSeen by SessionsTable.lastSeen
}
