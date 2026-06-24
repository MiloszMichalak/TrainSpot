package pl.meleko.trainspot.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commercial_categories")
data class CommercialCategoryEntity(
    @PrimaryKey val code: String,
    val name: String,
    val carrierCode: String?
)
