package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import pl.meleko.trainspot.database.UsersTable
import pl.meleko.trainspot.database.entities.UserEntity
import pl.meleko.trainspot.database.model.UserDto
import pl.meleko.trainspot.util.dbTransaction
import kotlin.uuid.Uuid

object UsersRepository {
    suspend fun findById(id: Uuid): UserDto? {
        return dbTransaction {
            UserEntity.findById(id)?.toDto()
        }
    }

    suspend fun findByEmailOrUsername(email: String = "", username: String = ""): UserDto? {
        return dbTransaction {
            UserEntity.find {
                (UsersTable.email eq email.lowercase()) or
                        (UsersTable.username eq username.lowercase())
            }.firstOrNull()?.toDto()
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

            UserEntity.new {
                this.email = email.lowercase()
                this.passwordHash = passwordHash
                this.avatarUrl = avatarUrl
                this.bio = bio
            }.id.value
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
            UserEntity.findById(id)?.apply {
                email?.let { this.email = it.lowercase() }
                username?.let { this.username = it.lowercase() }
                avatarUrl?.let { this.avatarUrl = it }
                bio?.let { this.bio = it }
            } != null
        }
    }

    suspend fun delete(id: Uuid): Boolean {
        return dbTransaction {
            UserEntity.findById(id)?.delete()
            true
        }
    }
}