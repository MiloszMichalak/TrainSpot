package pl.meleko.trainspot

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class User(
    val id: Uuid,
    val email: String,
    val username: String,
    val avatarUrl: String,
    val bio: String,
    val createdAt: Instant
)