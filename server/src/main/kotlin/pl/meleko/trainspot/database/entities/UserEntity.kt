package pl.meleko.trainspot.database.entities

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import pl.meleko.trainspot.database.LikesTable
import pl.meleko.trainspot.database.SessionsTable
import pl.meleko.trainspot.database.SpotsTable
import pl.meleko.trainspot.database.UsersTable
import pl.meleko.trainspot.database.model.UserDto
import kotlin.uuid.Uuid

class UserEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    companion object : UuidEntityClass<UserEntity>(UsersTable)

    var email by UsersTable.email
    var passwordHash by UsersTable.passwordHash
    var username by UsersTable.username
    var avatarUrl by UsersTable.avatarUrl
    var bio by UsersTable.bio
    var createdAt by UsersTable.createdAt

    val sessions by SessionEntity referrersOn SessionsTable.userId
    val spots by SpotEntity referrersOn SpotsTable.userId
    val likes by LikeEntity referrersOn LikesTable.userId

    fun toDto() = UserDto(
        id = id.value,
        email = email,
        username = username,
        avatarUrl = avatarUrl,
        passwordHash = passwordHash,
        bio = bio,
        createdAt = createdAt
    )
}
