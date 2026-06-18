package pl.meleko.trainspot.database.model

import org.jetbrains.exposed.v1.core.ResultRow
import pl.meleko.trainspot.database.SessionsTable
import java.time.OffsetDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class SessionDto(
    val sessionId: Uuid,
    val userId: Uuid,
    val createdAt: Instant,
    val lastSeen: OffsetDateTime
)

fun ResultRow.toSessionDto() = SessionDto(
    sessionId = this[SessionsTable.id].value,
    userId = this[SessionsTable.userId],
    createdAt = this[SessionsTable.createdAt],
    lastSeen = this[SessionsTable.lastSeen]
)