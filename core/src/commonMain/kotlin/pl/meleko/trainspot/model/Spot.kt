package pl.meleko.trainspot.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Spot(
    val id: Uuid,
    val user: User,
    val model: TrainModel,
    val station: Station?,
    val trainRun: ScheduleRoute?,
    val imageUrl: String,
    val description: String,
    val lat: Double?,
    val lon: Double?,
    val spottedAt: Instant,
    val createdAt: Instant,
    val likes: Long,
    val isLiked: Boolean = false,
    val commentsCount: Long = 0
)
