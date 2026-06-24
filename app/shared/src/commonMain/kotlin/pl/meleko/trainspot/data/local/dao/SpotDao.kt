package pl.meleko.trainspot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.meleko.trainspot.data.local.model.SpotEntity

@Dao
interface SpotDao {
    @Query("SELECT * FROM spots ORDER BY spottedAt DESC")
    fun getAllSpots(): Flow<List<SpotEntity>>

    @Query("SELECT * FROM spots WHERE id = :id")
    suspend fun getSpotById(id: Int): SpotEntity?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpot(spots: SpotEntity)

    @Query("DELETE FROM spots")
    suspend fun deleteAllSpots()
}
