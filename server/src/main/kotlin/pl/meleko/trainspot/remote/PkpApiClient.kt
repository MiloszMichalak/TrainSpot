package pl.meleko.trainspot.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pl.meleko.trainspot.remote.dto.CarrierDto
import pl.meleko.trainspot.remote.dto.CarriersResponseDto
import pl.meleko.trainspot.remote.dto.CommercialCategoriesResponseDto
import pl.meleko.trainspot.remote.dto.CommercialCategoryDto
import pl.meleko.trainspot.remote.dto.SchedulesResponseDto
import pl.meleko.trainspot.remote.dto.StationDto


object PkpApiClient {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
        install(DefaultRequest){
            url {
                host = "https://api.pkp.plk.pl"
            }

            header("X-Api-Key", "API_KEY")
        }
    }


    suspend fun fetchStations(): List<StationDto> {
        return client.get("/api/v1/dictionaries/stations") {
            parameter("pageSize", 10000)
        }.body<List<StationDto>>()
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
            parameter("fullRoute", true)
            parameter("dictionaries", false)
        }.body<SchedulesResponseDto>()
    }

    fun close() {
        client.close()
    }
}
