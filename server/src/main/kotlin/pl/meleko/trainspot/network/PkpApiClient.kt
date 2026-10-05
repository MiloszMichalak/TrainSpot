package pl.meleko.trainspot.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import pl.meleko.trainspot.network.dto.CarrierDto
import pl.meleko.trainspot.network.dto.CarriersResponse
import pl.meleko.trainspot.network.dto.CommercialCategoriesResponse
import pl.meleko.trainspot.network.dto.CommercialCategoryDto
import pl.meleko.trainspot.network.dto.SchedulesResponse
import pl.meleko.trainspot.network.dto.StationDto
import pl.meleko.trainspot.network.dto.StationsResponse

class PkpApiClient(
    private val client: HttpClient
) {
    suspend fun fetchStations(): List<StationDto> {
        return client.get("/api/v1/dictionaries/stations") {
            url {
                parameters.append("pageSize", "10000")
            }
        }.body<StationsResponse>().stations
    }

    suspend fun fetchCommercialCategories(): List<CommercialCategoryDto> {
        return client.get("/api/v1/dictionaries/commercial-categories")
            .body<CommercialCategoriesResponse>()
            .commercialCategories
    }

    suspend fun fetchCarriers(): List<CarrierDto> {
        return client.get("/api/v1/dictionaries/carriers")
            .body<CarriersResponse>()
            .carriers
    }

    suspend fun fetchSchedules(): SchedulesResponse {
        return client.get("/api/v1/schedules") {
            url {
                parameters.append("fullRoute", "true")
                parameters.append("dictionaries", "false")
            }
        }.body<SchedulesResponse>()
    }

    fun close() {
        client.close()
    }
}