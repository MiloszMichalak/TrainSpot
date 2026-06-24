package pl.meleko.trainspot.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carriers")
data class CarrierEntity(
    @PrimaryKey val code: String,
    val name: String,
    val validFrom: String,
    val validTo: String?
)
