package pl.meleko.trainspot.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Spot(
    val id: Uuid,
    val user: User,
    val model: TrainModel,
    val stationId: Int?,
    val trainRunId: Int,
    val imageUrl: String,
    val description: String,
    val lat: Double?,
    val lon: Double?,
    val spottedAt: Instant,
    val createdAt: Instant,
    val likes: Int
)