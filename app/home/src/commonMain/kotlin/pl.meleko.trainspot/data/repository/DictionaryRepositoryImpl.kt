package pl.meleko.trainspot.data.repository

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.core.onFailure
import pl.meleko.trainspot.core.onSuccess
import pl.meleko.trainspot.data.local.AuthDataStore
import pl.meleko.trainspot.data.local.dao.CarrierDao
import pl.meleko.trainspot.data.local.dao.CommercialCategoryDao
import pl.meleko.trainspot.data.local.dao.StationDao
import pl.meleko.trainspot.data.local.model.CarrierEntity
import pl.meleko.trainspot.data.local.model.CommercialCategoryEntity
import pl.meleko.trainspot.data.local.model.StationEntity
import pl.meleko.trainspot.data.remote.DictionaryService
import pl.meleko.trainspot.domain.DictionaryRepository
import pl.meleko.trainspot.model.Carrier
import pl.meleko.trainspot.model.CommercialCategory
import pl.meleko.trainspot.model.Station

class DictionaryRepositoryImpl(
    private val dictionaryService: DictionaryService,
    private val stationDao: StationDao,
    private val carrierDao: CarrierDao,
    private val categoryDao: CommercialCategoryDao,
    private val authDataStore: AuthDataStore
) : DictionaryRepository {
    override suspend fun syncDictionariesIfNeeded(): Result<Unit, DataError.Network> {
        if (authDataStore.isDictionarySynced()) {
            return Result.Success(Unit)
        }

        dictionaryService.getStations()
            .onSuccess { stations ->
                stationDao.insertStations(stations.map { it.toEntity() })
            }
            .onFailure {
                return Result.Error(it)
            }

        dictionaryService.getCarriers()
            .onSuccess { stations ->
                carrierDao.insertCarriers(stations.map { it.toEntity() })
            }
            .onFailure {
                return Result.Error(it)
            }


        dictionaryService.getCategories()
            .onSuccess { stations ->
                categoryDao.insertCategories(stations.map { it.toEntity() })
            }
            .onFailure {
                return Result.Error(it)
            }

        authDataStore.setDictionarySynced(true)

        return Result.Success(Unit)
    }

    override suspend fun searchStations(query: String): List<Station> {
        return stationDao.searchStations("%$query%")
            .map { it.toStation() }
    }

    override suspend fun findNearestStation(
        latitude: Double,
        longitude: Double,
        maxDistanceMeters: Double
    ): Station? = nearestStation(
        stations = stationDao.getStationsWithCoordinates().map { it.toStation() },
        latitude = latitude,
        longitude = longitude,
        maxDistanceMeters = maxDistanceMeters
    )

    private fun StationEntity.toStation(): Station {
        return Station(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude
        )
    }

    private fun Station.toEntity() = StationEntity(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude
    )

    private fun Carrier.toEntity() = CarrierEntity(
        code = code,
        name = name,
        validFrom = validFrom,
        validTo = validTo
    )
    private fun CommercialCategory.toEntity() = CommercialCategoryEntity(
        code = code,
        name = name,
        carrierCode = carrierCode
    )
}
