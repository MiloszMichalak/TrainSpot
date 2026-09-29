package pl.meleko.trainspot.domain

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.requests.SpotRequest
import pl.meleko.trainspot.response.PaginationResponse

interface SpotRepository {
    suspend fun getSpots(page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network>
    suspend fun getSpot(id: String): Result<Spot, DataError.Network>
    suspend fun createSpot(request: SpotRequest): Result<Spot, DataError.Network>
    suspend fun updateSpot(id: String, request: SpotRequest): Result<Spot, DataError.Network>
    suspend fun updateSpotImage(
        id: String,
        image: ByteArray,
        fileName: String = "spot.jpg",
        mimeType: String = "image/jpeg"
    ): Result<Spot, DataError.Network>
    suspend fun deleteSpot(id: String): Result<Unit, DataError.Network>
    suspend fun getUserSpots(userId: String, page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network>
    suspend fun getMySpots(page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network>
}
