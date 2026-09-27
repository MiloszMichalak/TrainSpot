package pl.meleko.trainspot.data.repository

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.data.remote.LikeService
import pl.meleko.trainspot.domain.LikeRepository
import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.response.PaginationResponse

class LikeRepositoryImpl(
    private val likeService: LikeService
) : LikeRepository {
    override suspend fun likeSpot(spotId: String): Result<Unit, DataError.Network> {
        return likeService.likeSpot(spotId)
    }

    override suspend fun unlikeSpot(spotId: String): Result<Unit, DataError.Network> {
        return likeService.unlikeSpot(spotId)
    }

    override suspend fun getUserLikedSpots(userId: String, page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network> {
        return likeService.getUserLikedSpots(userId, page, limit)
    }

    override suspend fun getMyLikedSpots(page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network> {
        return likeService.getMyLikedSpots(page, limit)
    }
}
