package pl.meleko.trainspot.service

import io.ktor.http.HttpStatusCode
import pl.meleko.trainspot.network.dto.ScheduleRouteDto
import pl.meleko.trainspot.repository.PkpRepository
import pl.meleko.trainspot.util.NetworkResult

object ScheduleService {
    suspend fun getRecentTrains(stationId: Int?, minutes: Long): NetworkResult<List<ScheduleRouteDto>> {
        if (stationId == null) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }

        return try {
            NetworkResult.Success(PkpRepository.getRecentTrainsForStation(stationId, minutes))
        } catch (e: Exception) {
            NetworkResult.Error(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun getTodayTrainsForStation(stationId: Int?): NetworkResult<List<ScheduleRouteDto>> {
        if (stationId == null) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }

        return try {
            NetworkResult.Success(PkpRepository.getTodayTrainsForStation(stationId))
        } catch (e: Exception) {
            NetworkResult.Error(HttpStatusCode.InternalServerError)
        }
    }
}
