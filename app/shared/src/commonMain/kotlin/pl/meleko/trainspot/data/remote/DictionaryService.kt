package pl.meleko.trainspot.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.Carrier
import pl.meleko.trainspot.model.CommercialCategory
import pl.meleko.trainspot.model.Station

class DictionaryService(private val client: HttpClient) {
    suspend fun getStations(): Result<List<Station>, DataError.Network> {
        return safeCall {
            client.get("/dictionary/stations")
        }
    }

    suspend fun getCarriers(): Result<List<Carrier>, DataError.Network> {
        return safeCall {
            client.get("/dictionary/carriers")
        }
    }

    suspend fun getCategories(): Result<List<CommercialCategory>, DataError.Network> {
        return safeCall {
            client.get("/dictionary/categories")
        }
    }
}
