package pl.meleko.trainspot.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey val id: Int,
    val name: String
)
