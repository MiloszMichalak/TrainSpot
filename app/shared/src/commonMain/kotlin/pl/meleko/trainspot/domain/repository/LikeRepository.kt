package pl.meleko.trainspot.domain.repository

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.response.PaginationResponse

interface LikeRepository {
    suspend fun likeSpot(spotId: String): Result<Unit, DataError.Network>
    suspend fun unlikeSpot(spotId: String): Result<Unit, DataError.Network>
    suspend fun getUserLikedSpots(userId: String, page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network>
    suspend fun getMyLikedSpots(page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network>
}
