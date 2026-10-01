package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import pl.meleko.trainspot.database.SessionsTable
import pl.meleko.trainspot.database.UsersTable
import pl.meleko.trainspot.util.dbTransaction
import java.time.OffsetDateTime
import kotlin.uuid.Uuid

object SessionsRepository {
    suspend fun create(userId: Uuid): Uuid {
        return dbTransaction {
            val userExists = UsersTable
                .selectAll()
                .where { UsersTable.id eq userId }
                .any()

            if (!userExists) throw IllegalArgumentException("User not found")

            SessionsTable.insertAndGetId { it[SessionsTable.userId] = userId }.value
        }
    }

    suspend fun delete(id: Uuid): Boolean {
        return dbTransaction {
            SessionsTable
                .deleteWhere { SessionsTable.id eq id } > 0
        }
    }

    suspend fun deleteAllByUserId(userId: Uuid): Int {
        return dbTransaction {
            SessionsTable
                .deleteWhere { SessionsTable.userId eq userId }
        }
    }

    suspend fun updateLastSeen(sessionId: Uuid): Boolean {
        return dbTransaction {
            SessionsTable
                .update({ SessionsTable.id eq sessionId }) {
                    it[lastSeen] = OffsetDateTime.now()
                } > 0
        }
    }
}