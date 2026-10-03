package pl.meleko.trainspot.domain

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.Station

interface DictionaryRepository {
    suspend fun syncDictionariesIfNeeded(): Result<Unit, DataError.Network>
    suspend fun searchStations(query: String): List<Station>
    suspend fun findNearestStation(
        latitude: Double,
        longitude: Double,
        maxDistanceMeters: Double
    ): Station?
}
