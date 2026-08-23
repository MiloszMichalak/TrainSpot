package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.eq
import pl.meleko.trainspot.database.SessionsTable
import pl.meleko.trainspot.database.entities.SessionEntity
import pl.meleko.trainspot.database.entities.UserEntity
import pl.meleko.trainspot.util.dbTransaction
import java.time.OffsetDateTime
import kotlin.uuid.Uuid

object SessionsRepository {
    suspend fun create(userId: Uuid): Uuid {
        return dbTransaction {
            val user = UserEntity.findById(userId) ?: throw IllegalArgumentException("User not found")
            SessionEntity.new {
                this.user = user
            }.id.value
        }
    }

    suspend fun delete(id: Uuid): Boolean {
        return dbTransaction {
            SessionEntity.findById(id)?.delete()
            true
        }
    }

    suspend fun deleteAllByUserId(userId: Uuid): Int {
        return dbTransaction {
            val sessions = SessionEntity.find { SessionsTable.userId eq userId }
            val count = sessions.count().toInt()
            sessions.forEach { it.delete() }
            count
        }
    }

    suspend fun updateLastSeen(sessionId: Uuid): Boolean {
        return dbTransaction {
            SessionEntity.findById(sessionId)?.apply {
                this.lastSeen = OffsetDateTime.now()
            } != null
        }
    }
}