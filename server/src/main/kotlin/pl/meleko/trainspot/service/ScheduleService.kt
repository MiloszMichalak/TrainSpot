package pl.meleko.trainspot.service

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import pl.meleko.trainspot.model.ScheduleRoute
import pl.meleko.trainspot.model.ScheduleRouteStops
import pl.meleko.trainspot.network.dto.toScheduleRoute
import pl.meleko.trainspot.repository.PkpRepository
import pl.meleko.trainspot.util.NetworkResult

object ScheduleService {
    suspend fun getTrainRoute(trainOrderId: Int?): NetworkResult<ScheduleRouteStops> {
        if (trainOrderId == null || trainOrderId <= 0) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }
        return try {
            val route = PkpRepository.getTrainRouteById(trainOrderId)
                ?: return NetworkResult.Error(HttpStatusCode.NotFound)
            NetworkResult.Success(route)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NetworkResult.Error(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun getRecentTrains(stationId: Int?, minutes: Long): NetworkResult<List<ScheduleRoute>> {
        if (stationId == null) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }

        return try {
            NetworkResult.Success(PkpRepository.getRecentTrainsForStation(stationId, minutes).map { it.toScheduleRoute() })
        } catch (e: Exception) {
            NetworkResult.Error(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun getTodayTrainsForStation(stationId: Int?): NetworkResult<List<ScheduleRoute>> {
        if (stationId == null) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }

        return try {
            NetworkResult.Success(PkpRepository.getTodayTrainsForStation(stationId).map { it.toScheduleRoute() })
        } catch (e: Exception) {
            NetworkResult.Error(HttpStatusCode.InternalServerError)
        }
    }
}
