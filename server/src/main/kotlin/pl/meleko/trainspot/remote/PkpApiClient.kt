package pl.meleko.trainspot.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters
import pl.meleko.trainspot.remote.dto.CarrierDto
import pl.meleko.trainspot.remote.dto.CarriersResponseDto
import pl.meleko.trainspot.remote.dto.CommercialCategoriesResponseDto
import pl.meleko.trainspot.remote.dto.CommercialCategoryDto
import pl.meleko.trainspot.remote.dto.SchedulesResponseDto
import pl.meleko.trainspot.remote.dto.StationDto
import pl.meleko.trainspot.remote.dto.StationsResponseDto


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
