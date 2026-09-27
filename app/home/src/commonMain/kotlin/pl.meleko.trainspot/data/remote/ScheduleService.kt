package pl.meleko.trainspot.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.ScheduleRoute

class ScheduleService(private val client: HttpClient) {
    suspend fun getRecentTrains(stationId: Int, minutes: Long = 60): Result<List<ScheduleRoute>, DataError.Network> {
        return safeCall {
            client.get("/schedule/station/$stationId/recent") {
                parameter("minutes", minutes)
            }
        }
    }
}
