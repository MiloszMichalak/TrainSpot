package pl.meleko.trainspot.database.model

import java.time.OffsetDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class SessionDto(
    val sessionId: Uuid,
    val userId: Uuid,
    val createdAt: Instant,
    val lastSeen: OffsetDateTime
)