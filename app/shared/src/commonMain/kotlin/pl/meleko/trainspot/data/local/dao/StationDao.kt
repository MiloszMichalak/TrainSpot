package pl.meleko.trainspot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.meleko.trainspot.data.local.model.StationEntity

@Dao
interface StationDao {
    @Query("SELECT * FROM stations")
    fun getAllStations(): Flow<List<StationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<StationEntity>)

    @Query("SELECT * FROM stations WHERE name LIKE :query AND latitude IS NOT NULL AND longitude IS NOT NULL LIMIT 20")
    suspend fun searchStations(query: String): List<StationEntity>
}
