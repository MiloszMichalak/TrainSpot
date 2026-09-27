package pl.meleko.trainspot.data.repository

import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.data.remote.SpotService
import pl.meleko.trainspot.domain.SpotRepository
import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.requests.SpotRequest
import pl.meleko.trainspot.response.PaginationResponse

class SpotRepositoryImpl(
    private val spotService: SpotService
) : SpotRepository {
    override suspend fun getSpots(page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network> {
        return spotService.getSpots(page, limit)
    }

    override suspend fun getSpot(id: String): Result<Spot, DataError.Network> {
        return spotService.getSpot(id)
    }

    override suspend fun createSpot(request: SpotRequest): Result<Spot, DataError.Network> {
        return spotService.createSpot(request)
    }

    override suspend fun updateSpot(id: String, request: SpotRequest): Result<Spot, DataError.Network> {
        return spotService.updateSpot(id, request)
    }

    override suspend fun updateSpotImage(id: String, image: ByteArray): Result<Spot, DataError.Network> {
        return spotService.updateSpotImage(id, image)
    }

    override suspend fun deleteSpot(id: String): Result<Unit, DataError.Network> {
        return spotService.deleteSpot(id)
    }

    override suspend fun getUserSpots(userId: String, page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network> {
        return spotService.getUserSpots(userId, page, limit)
    }

    override suspend fun getMySpots(page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network> {
        return spotService.getMySpots(page, limit)
    }
}
