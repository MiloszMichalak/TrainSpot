package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import pl.meleko.trainspot.database.SessionsTable
import pl.meleko.trainspot.database.model.SessionDto
import pl.meleko.trainspot.database.model.toSessionDto
import java.time.OffsetDateTime
import kotlin.uuid.Uuid

object SessionsRepository {
    fun create(userId: Uuid): Uuid = transaction {
        SessionsTable.insert {
            it[SessionsTable.userId] = userId
        }[SessionsTable.id].value
    }

    fun findById(id: Uuid): SessionDto? = transaction {
        SessionsTable.selectAll()
            .where { SessionsTable.id eq id }
            .firstOrNull()
            ?.toSessionDto()
    }

    fun findLatestByUserId(userId: Uuid): SessionDto? = transaction {
        SessionsTable.selectAll()
            .where { SessionsTable.userId eq userId }
            .orderBy(SessionsTable.createdAt to SortOrder.DESC)
            .firstOrNull()
            ?.toSessionDto()
    }

    fun findAllByUserId(userId: Uuid): List<SessionDto> = transaction {
        SessionsTable.selectAll()
            .where { SessionsTable.userId eq userId }
            .orderBy(SessionsTable.createdAt to SortOrder.DESC)
            .map { it.toSessionDto() }
    }

    fun delete(id: Uuid): Boolean = transaction {
        SessionsTable.deleteWhere { SessionsTable.id eq id } > 0
    }

    fun deleteAllByUserId(userId: Uuid): Int = transaction {
        SessionsTable.deleteWhere { SessionsTable.userId eq userId }
    }

    fun updateLastSeen(sessionId: Uuid): Boolean = transaction {
        SessionsTable.update({ SessionsTable.id eq sessionId }) { row ->
            row[SessionsTable.lastSeen] = OffsetDateTime.now()
        } > 0
    }
}