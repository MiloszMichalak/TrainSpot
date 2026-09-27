package pl.meleko.trainspot.domain

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.ScheduleRoute

interface ScheduleRepository {
    suspend fun getRecentTrains(stationId: Int): Result<List<ScheduleRoute>, DataError.Network>
}
