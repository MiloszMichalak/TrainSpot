package pl.meleko.trainspot.service

import io.ktor.http.HttpStatusCode
import pl.meleko.trainspot.network.dto.ScheduleRouteDto
import pl.meleko.trainspot.repository.PkpRepository
import pl.meleko.trainspot.util.NetworkResult

object ScheduleService {
    fun getRecentTrains(stationId: Int, minutes: Long): NetworkResult<List<ScheduleRouteDto>> {
        return try {
            NetworkResult.Success(PkpRepository.getRecentTrainsForStation(stationId, minutes))
        } catch (e: Exception) {
            NetworkResult.Error(HttpStatusCode.InternalServerError)
        }
    }

    fun getTodayTrainsForStation(stationId: Int): NetworkResult<List<ScheduleRouteDto>> {
        return try {
            NetworkResult.Success(PkpRepository.getTodayTrainsForStation(stationId))
        } catch (e: Exception) {
            NetworkResult.Error(HttpStatusCode.InternalServerError)
        }
    }
}
