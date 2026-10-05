package pl.meleko.trainspot.domain

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.ScheduleRoute
import pl.meleko.trainspot.model.ScheduleRouteStops

interface ScheduleRepository {
    suspend fun getTrainRoute(trainOrderId: Int): Result<ScheduleRouteStops, DataError.Network>
    suspend fun getRecentTrains(stationId: Int): Result<List<ScheduleRoute>, DataError.Network>
}
