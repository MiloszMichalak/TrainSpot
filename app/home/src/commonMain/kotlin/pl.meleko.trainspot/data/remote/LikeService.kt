package pl.meleko.trainspot.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.response.PaginationResponse

class LikeService(private val client: HttpClient) {
    suspend fun likeSpot(spotId: String): Result<Unit, DataError.Network> {
        return safeCall {
            client.post("/likes/spots/$spotId")
        }
    }

    suspend fun unlikeSpot(spotId: String): Result<Unit, DataError.Network> {
        return safeCall {
            client.delete("/likes/spots/$spotId")
        }
    }

    suspend fun getUserLikedSpots(userId: String, page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network> {
        return safeCall {
            client.get("/likes/user/$userId") {
                parameter("page", page)
                parameter("limit", limit)
            }
        }
    }

    suspend fun getMyLikedSpots(page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network> {
        return safeCall {
            client.get("/likes/user/me") {
                parameter("page", page)
                parameter("limit", limit)
            }
        }
    }
}
