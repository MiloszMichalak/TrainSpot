package pl.meleko.trainspot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.meleko.trainspot.data.local.model.CarrierEntity

@Dao
interface CarrierDao {
    @Query("SELECT * FROM carriers")
    fun getAllCarriers(): Flow<List<CarrierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarriers(carriers: List<CarrierEntity>)
}
