package pl.meleko.trainspot.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spots")
data class SpotEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String,
    val trainModel: String,
    val trainNumber: String,
    val carrierCode: String,
    val stationId: Int?,
    val trainRunId: String,
    val imageUrl: String,
    val description: String,
    val lat: Double?,
    val lon: Double?,
    val spottedAt: Long,
    val createdAt: Long,
    val likes: Int
)
