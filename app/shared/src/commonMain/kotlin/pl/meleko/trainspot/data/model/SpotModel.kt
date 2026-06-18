// Shared Spot model — used by both Android and iOS to deserialize SpotResponse DTO.
// This file lives in the shared module so both platforms have the same model definition.
// Do not modify this file without updating both platforms' deserialization logic.

package pl.meleko.trainspot.data.model

import kotlinx.serialization.Serializable
import java.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class SpotModel(
    val id: Uuid,
    val user: UserModel,
    val modelId: Uuid?,
    val stationId: Int?,
    val trainRunId: Int?,
    val imageUrl: String,
    val description: String?,
    val lat: Double?,
    val lon: Double?,
    val spottedAt: Instant,
    val createdAt: Instant,
    val likes: Int,
)