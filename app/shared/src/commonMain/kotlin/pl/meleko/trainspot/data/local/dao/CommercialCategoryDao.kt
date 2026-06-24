package pl.meleko.trainspot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.meleko.trainspot.data.local.model.CommercialCategoryEntity

@Dao
interface CommercialCategoryDao {
    @Query("SELECT * FROM commercial_categories")
    fun getAllCategories(): Flow<List<CommercialCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CommercialCategoryEntity>)
}
