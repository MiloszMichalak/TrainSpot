package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import pl.meleko.trainspot.database.SessionsTable
import pl.meleko.trainspot.database.UsersTable
import pl.meleko.trainspot.database.model.SessionDto
import pl.meleko.trainspot.database.model.UserDto
import pl.meleko.trainspot.database.model.toUserDto
import kotlin.uuid.Uuid

object UsersRepository {
    fun findById(id: Uuid): UserDto? = transaction {
        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .firstOrNull()
            ?.toUserDto()
    }

    fun findByEmailOrUsername(email: String = "", username: String = ""): UserDto? = transaction {
        UsersTable.selectAll()
            .where {
                (UsersTable.email eq email.lowercase()) or
                        (UsersTable.username eq username.lowercase())
            }
            .firstOrNull()
            ?.toUserDto()
    }

    fun insert(
        email: String,
        passwordHash: String,
        username: String,
        avatarUrl: String? = null,
        bio: String? = null
    ): Uuid? = transaction {
        val conflict = findByEmailOrUsername(email, username)
        if (conflict != null) return@transaction null

        UsersTable.insert {
            it[UsersTable.email] = email.lowercase()
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.username] = username.lowercase()
            it[UsersTable.avatarUrl] = avatarUrl
            it[UsersTable.bio] = bio
        }[UsersTable.id].value
    }

    fun update(
        id: Uuid,
        email: String? = null,
        username: String? = null,
        avatarUrl: String? = null,
        bio: String? = null
    ): Boolean = transaction {
        UsersTable.update({ UsersTable.id eq id }) { row ->
            email?.let { row[UsersTable.email] = it.lowercase() }
            username?.let { row[UsersTable.username] = it.lowercase() }
            avatarUrl?.let { row[UsersTable.avatarUrl] = it }
            bio?.let { row[UsersTable.bio] = it }
        } > 0
    }

    fun delete(id: Uuid): Boolean = transaction {
        UsersTable.deleteWhere { UsersTable.id eq id } > 0
    }
}

fun ResultRow.toSessionDto() = SessionDto(
    sessionId = this[SessionsTable.id].value,
    userId = this[SessionsTable.userId],
    createdAt = this[SessionsTable.createdAt],
    lastSeen = this[SessionsTable.lastSeen]
)