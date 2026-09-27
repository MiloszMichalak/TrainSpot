package pl.meleko.trainspot.data.repository

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.data.remote.ScheduleService
import pl.meleko.trainspot.domain.ScheduleRepository
import pl.meleko.trainspot.model.ScheduleRoute

class ScheduleRepositoryImpl(
    private val scheduleService: ScheduleService
) : ScheduleRepository {
    override suspend fun getRecentTrains(stationId: Int): Result<List<ScheduleRoute>, DataError.Network> {
        return scheduleService.getRecentTrains(stationId)
    }
}
