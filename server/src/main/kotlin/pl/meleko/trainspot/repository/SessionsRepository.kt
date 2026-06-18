package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import pl.meleko.trainspot.database.SessionsTable
import pl.meleko.trainspot.database.model.SessionDto
import pl.meleko.trainspot.database.model.toSessionDto
import pl.meleko.trainspot.util.dbTransaction
import java.time.OffsetDateTime
import kotlin.uuid.Uuid

object SessionsRepository {
    suspend fun create(userId: Uuid): Uuid {
        return dbTransaction {
            SessionsTable.insert {
                it[SessionsTable.userId] = userId
            }[SessionsTable.id].value
        }
    }

    suspend fun findById(id: Uuid): SessionDto? {
        return dbTransaction {
            SessionsTable.selectAll()
                .where { SessionsTable.id eq id }
                .firstOrNull()
                ?.toSessionDto()
        }
    }

    suspend fun findLatestByUserId(userId: Uuid): SessionDto? {
        return dbTransaction {
            SessionsTable.selectAll()
                .where { SessionsTable.userId eq userId }
                .orderBy(SessionsTable.createdAt to SortOrder.DESC)
                .firstOrNull()
                ?.toSessionDto()
        }
    }

    suspend fun findAllByUserId(userId: Uuid): List<SessionDto> {
        return dbTransaction {
            SessionsTable.selectAll()
                .where { SessionsTable.userId eq userId }
                .orderBy(SessionsTable.createdAt to SortOrder.DESC)
                .map { it.toSessionDto() }
        }
    }

    suspend fun delete(id: Uuid): Boolean {
        return dbTransaction {
            SessionsTable.deleteWhere { SessionsTable.id eq id } > 0
        }
    }

    suspend fun deleteAllByUserId(userId: Uuid): Int {
        return dbTransaction {
            SessionsTable.deleteWhere { SessionsTable.userId eq userId }
        }
    }

    suspend fun updateLastSeen(sessionId: Uuid): Boolean {
        return dbTransaction {
            SessionsTable.update({ SessionsTable.id eq sessionId }) { row ->
                row[SessionsTable.lastSeen] = OffsetDateTime.now()
            } > 0
        }
    }
}