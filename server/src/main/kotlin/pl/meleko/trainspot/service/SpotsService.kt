package pl.meleko.trainspot.service

import io.ktor.http.HttpStatusCode
import pl.meleko.trainspot.PaginationResponse
import pl.meleko.trainspot.SpotResponse
import pl.meleko.trainspot.database.model.SpotDto
import pl.meleko.trainspot.database.model.toSpotResponse
import pl.meleko.trainspot.repository.LikesRepository
import pl.meleko.trainspot.repository.SpotRepository
import pl.meleko.trainspot.requests.SpotRequest
import pl.meleko.trainspot.util.ImageStorage
import pl.meleko.trainspot.util.IncomingImage
import pl.meleko.trainspot.util.NetworkResult
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * SpotsService - handles all spot-related operations with validation
 */
object SpotsService {

    @OptIn(ExperimentalUuidApi::class)
    fun create(userId: Uuid, request: SpotRequest, incomingImage: IncomingImage?): NetworkResult<SpotResponse> {
        if (incomingImage == null) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }

        request.lat?.let { lat ->
            if (lat < -90.0 || lat > 90.0) {
                return NetworkResult.Error(HttpStatusCode.BadRequest)
            }
        }

        request.lon?.let { lon ->
            if (lon < -180.0 || lon > 180.0) {
                return NetworkResult.Error(HttpStatusCode.BadRequest)
            }
        }

        // Save image to disk — the image is written synchronously before the DB transaction.
        val imageUrl = ImageStorage.save(
            image = incomingImage,
            subdir = "spots",
        )

        // Now create the DB record with the pre-generated ID.
        val createdId = SpotRepository.create(userId, request, imageUrl, spotId)
            ?: return NetworkResult.Error(HttpStatusCode.Conflict)

        val spot = SpotRepository.findById(createdId)
            ?: return NetworkResult.Error(HttpStatusCode.InternalServerError)

        return NetworkResult.Success(spot)
    }

    fun getById(id: Uuid): NetworkResult<SpotResponse> {
        val spot = SpotRepository.findById(id)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        return NetworkResult.Success(spot)
    }

    fun findAll(page: Int = 0, limit: Int = 20): NetworkResult<PaginationResponse<SpotResponse>> {
        if (page < 0 || limit < 1 || limit > 100) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }
        val result = SpotRepository.findAll(page, limit)
        return NetworkResult.Success(result)
    }

    fun findByUser(userId: Uuid, page: Int = 0, limit: Int = 20): NetworkResult<PaginationResponse<SpotResponse>> {
        if (page < 0 || limit < 1 || limit > 100) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }
        val result = SpotRepository.findByUser(userId, page, limit)
        return NetworkResult.Success(result)
    }

    fun update(id: Uuid, userId: Uuid, request: SpotRequest, imageUrl: String?): NetworkResult<SpotResponse> {
        val existingSpot = SpotRepository.findById(id)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)

        if (existingSpot.userId != userId) {
            return NetworkResult.Error(HttpStatusCode.Forbidden)
        }

        if (imageUrl == null) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }

        // Validate lat/lon if provided
        request.lat?.let { lat ->
            if (lat < -90.0 || lat > 90.0) {
                return NetworkResult.Error(HttpStatusCode.BadRequest)
            }
        }
        request.lon?.let { lon ->
            if (lon < -180.0 || lon > 180.0) {
                return NetworkResult.Error(HttpStatusCode.BadRequest)
            }
        }

        val updatedId = SpotRepository.update(id, request, imageUrl)
            ?: return NetworkResult.Error(HttpStatusCode.Conflict)

        val updatedSpot = SpotRepository.findById(id)?.toSpotResponse()
            ?: return NetworkResult.Error(HttpStatusCode.InternalServerError)

        return NetworkResult.Success(updatedSpot)
    }

    fun delete(id: Uuid, userId: Uuid): NetworkResult<Unit> {
        val existingSpot = SpotRepository.findById(id)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)

        if (existingSpot.userId != userId) {
            return NetworkResult.Error(HttpStatusCode.Forbidden)
        }

        val deleted = SpotRepository.delete(id)
        return if (deleted) {
            NetworkResult.Success(Unit)
        } else {
            NetworkResult.Error(HttpStatusCode.NotFound)
        }
    }

    fun getLikedSpots(userId: Uuid, page: Int = 0, limit: Int = 20): NetworkResult<PaginationResponse<SpotDto>> {
        if (page < 0 || limit < 1 || limit > 100) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }

        val likedSpotIds = LikesRepository.findByUserId(userId).toSet()

        val result = SpotRepository.findAll(page, limit)
        val spots = result.items.filter { likedSpotIds.contains(it.id) }
        val total = spots.size

        return NetworkResult.Success(
            PaginationResponse(
                items = spots,
                total = total,
                page = page,
                limit = limit,
                totalPages = 1
            )
        )
    }
}