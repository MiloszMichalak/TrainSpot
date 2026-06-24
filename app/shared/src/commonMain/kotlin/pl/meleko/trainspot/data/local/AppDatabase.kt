package pl.meleko.trainspot.data.local

import androidx.room.*
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import pl.meleko.trainspot.data.local.dao.CarrierDao
import pl.meleko.trainspot.data.local.dao.CommercialCategoryDao
import pl.meleko.trainspot.data.local.dao.SpotDao
import pl.meleko.trainspot.data.local.dao.StationDao
import pl.meleko.trainspot.data.local.model.CarrierEntity
import pl.meleko.trainspot.data.local.model.CommercialCategoryEntity
import pl.meleko.trainspot.data.local.model.SpotEntity
import pl.meleko.trainspot.data.local.model.StationEntity

@Database(
    entities = [
        SpotEntity::class,
        StationEntity::class,
        CarrierEntity::class,
        CommercialCategoryEntity::class
    ],
    version = 1
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spotDao(): SpotDao
    abstract fun stationDao(): StationDao
    abstract fun carrierDao(): CarrierDao
    abstract fun categoryDao(): CommercialCategoryDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
