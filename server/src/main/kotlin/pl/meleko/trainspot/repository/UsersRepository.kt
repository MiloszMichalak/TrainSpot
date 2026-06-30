package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import pl.meleko.trainspot.database.UsersTable
import pl.meleko.trainspot.database.model.UserDto
import pl.meleko.trainspot.database.model.toUserDto
import pl.meleko.trainspot.util.dbTransaction
import kotlin.uuid.Uuid

object UsersRepository {
    suspend fun findById(id: Uuid): UserDto? {
        return dbTransaction {
            UsersTable.selectAll()
                .where { UsersTable.id eq id }
                .firstOrNull()
                ?.toUserDto()
        }
    }

    suspend fun findByEmailOrUsername(email: String = "", username: String = ""): UserDto? {
        return dbTransaction {
            UsersTable.selectAll()
                .where {
                    (UsersTable.email eq email.lowercase()) or
                            (UsersTable.username eq username.lowercase())
                }
                .firstOrNull()
                ?.toUserDto()
        }
    }

    suspend fun insert(
        email: String,
        passwordHash: String,
        avatarUrl: String? = null,
        bio: String? = null
    ): Uuid? {
        return dbTransaction {
            val conflict = findByEmailOrUsername(email)
            if (conflict != null) return@dbTransaction null

            UsersTable.insert {
                it[UsersTable.email] = email.lowercase()
                it[UsersTable.passwordHash] = passwordHash
                it[UsersTable.avatarUrl] = avatarUrl
                it[UsersTable.bio] = bio
            }[UsersTable.id].value
        }
    }

    suspend fun update(
        id: Uuid,
        email: String? = null,
        username: String? = null,
        avatarUrl: String? = null,
        bio: String? = null
    ): Boolean {
        return dbTransaction {
            UsersTable.update({ UsersTable.id eq id }) { row ->
                email?.let { row[UsersTable.email] = it.lowercase() }
                username?.let { row[UsersTable.username] = it.lowercase() }
                avatarUrl?.let { row[UsersTable.avatarUrl] = it }
                bio?.let { row[UsersTable.bio] = it }
            } > 0
        }
    }

    suspend fun delete(id: Uuid): Boolean {
        return dbTransaction {
            UsersTable.deleteWhere { UsersTable.id eq id } > 0
        }
    }
}