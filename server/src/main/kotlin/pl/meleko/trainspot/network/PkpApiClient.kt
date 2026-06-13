package pl.meleko.trainspot.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters
import pl.meleko.trainspot.network.dto.CarrierDto
import pl.meleko.trainspot.network.dto.CarriersResponseDto
import pl.meleko.trainspot.network.dto.CommercialCategoriesResponseDto
import pl.meleko.trainspot.network.dto.CommercialCategoryDto
import pl.meleko.trainspot.network.dto.SchedulesResponseDto
import pl.meleko.trainspot.network.dto.StationDto
import pl.meleko.trainspot.network.dto.StationsResponseDto

class PkpApiClient(
    private val client: HttpClient
) {
    suspend fun fetchStations(): List<StationDto> {
        return client.get("/api/v1/dictionaries/stations") {
            url {
                parameters.append("pageSize", "10000")
            }
        }.body<StationsResponseDto>().stations
    }

    suspend fun fetchCommercialCategories(): List<CommercialCategoryDto> {
        return client.get("/api/v1/dictionaries/commercial-categories")
            .body<CommercialCategoriesResponseDto>()
            .commercialCategories
    }

    suspend fun fetchCarriers(): List<CarrierDto> {
        return client.get("/api/v1/dictionaries/carriers")
            .body<CarriersResponseDto>()
            .carriers
    }

    suspend fun fetchSchedules(): SchedulesResponseDto {
        return client.get("/api/v1/schedules") {
            url {
                parameters {
                    append("fullRoute", "true")
                    append("dictionaries", "false")
                }
            }
        }.body<SchedulesResponseDto>()
    }

    fun close() {
        client.close()
    }
}