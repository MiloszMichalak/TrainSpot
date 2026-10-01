package pl.meleko.trainspot.service

import io.ktor.http.HttpStatusCode
import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.repository.LikesRepository
import pl.meleko.trainspot.repository.SpotRepository
import pl.meleko.trainspot.requests.SpotRequest
import pl.meleko.trainspot.response.PaginationResponse
import pl.meleko.trainspot.util.ImageStorage
import pl.meleko.trainspot.util.IncomingImage
import pl.meleko.trainspot.util.NetworkResult
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object SpotsService {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun create(userId: Uuid, request: SpotRequest): NetworkResult<Spot> {
        val trainModel = request.trainModel
        if (
            trainModel == null ||
            request.trainRunId == null
        ) {
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

        val spotId = Uuid.generateV4()
        val createdId = SpotRepository.create(userId, request, spotId)
            ?: return NetworkResult.Error(HttpStatusCode.BadRequest)

        val spot = SpotRepository.findById(createdId)
            ?: return NetworkResult.Error(HttpStatusCode.InternalServerError)

        return NetworkResult.Success(spot)
    }

    suspend fun getById(id: Uuid): NetworkResult<Spot> {
        val spot = SpotRepository.findById(id)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        return NetworkResult.Success(spot)
    }

    suspend fun findAll(page: Int = 0, limit: Int = 20): NetworkResult<PaginationResponse<Spot>> {
        if (page < 0 || limit < 1 || limit > 100) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }

        val result = SpotRepository.findAll(page, limit)
        return NetworkResult.Success(result)
    }

    suspend fun findByUser(userId: Uuid, page: Int = 0, limit: Int = 20): NetworkResult<PaginationResponse<Spot>> {
        if (page < 0 || limit < 1 || limit > 100) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }
        val result = SpotRepository.findByUser(userId, page, limit)
        return NetworkResult.Success(result)
    }

    suspend fun update(id: Uuid, userId: Uuid, request: SpotRequest): NetworkResult<Spot> {
        val existingSpot = SpotRepository.findById(id)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)

        if (existingSpot.user.id != userId) {
            return NetworkResult.Error(HttpStatusCode.Forbidden)
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

        SpotRepository.update(id, request)
            ?: return NetworkResult.Error(HttpStatusCode.Conflict)

        val updatedSpot = SpotRepository.findById(id)
            ?: return NetworkResult.Error(HttpStatusCode.InternalServerError)

        return NetworkResult.Success(updatedSpot)
    }

    suspend fun uploadImage(id: Uuid, userId: Uuid, incomingImage: IncomingImage?): NetworkResult<Spot> {
        val existingSpot = SpotRepository.findById(id)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        if (existingSpot.user.id != userId) {
            return NetworkResult.Error(HttpStatusCode.Forbidden)
        }
        if (incomingImage == null || incomingImage.bytes.isEmpty()) {
            return NetworkResult.Error(HttpStatusCode.BadRequest)
        }

        val imageUrl = try {
            ImageStorage.save(incomingImage, id = id, subdir = "spots")
        } catch (_: Exception) {
            return NetworkResult.Error(HttpStatusCode.InternalServerError)
        }

        if (SpotRepository.updateImage(id, imageUrl) != true) {
            ImageStorage.delete(imageUrl)
            return NetworkResult.Error(HttpStatusCode.Conflict)
        }

        val updatedSpot = SpotRepository.findById(id)
            ?: return NetworkResult.Error(HttpStatusCode.InternalServerError)
        return NetworkResult.Success(updatedSpot)
    }

    suspend fun delete(id: Uuid, userId: Uuid): NetworkResult<Unit> {
        val existingSpot = SpotRepository.findById(id)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)

        if (existingSpot.user.id != userId) {
            return NetworkResult.Error(HttpStatusCode.Forbidden)
        }

        val deleted = SpotRepository.delete(id)
        return if (deleted) {
            NetworkResult.Success(Unit)
        } else {
            NetworkResult.Error(HttpStatusCode.NotFound)
        }
    }

     suspend fun getLikedSpots(userId: Uuid, page: Int = 0, limit: Int = 20): NetworkResult<PaginationResponse<Spot>> {
         if (page < 0 || limit < 1 || limit > 100) {
             return NetworkResult.Error(HttpStatusCode.BadRequest)
         }

         val total = LikesRepository.countByUserId(userId).toInt()
         val likedSpotIds = LikesRepository.findLikedSpotIdsByUserIdPaginated(userId, page, limit)
         
         if (likedSpotIds.isEmpty()) {
             return NetworkResult.Success(
                 PaginationResponse(
                     items = emptyList(),
                     total = 0,
                     page = page,
                     limit = limit,
                     totalPages = 0
                 )
             )
         }

         val spots = SpotRepository.findByIds(likedSpotIds)

         return NetworkResult.Success(
             PaginationResponse(
                 items = spots,
                 total = total,
                 page = page,
                 limit = limit,
                 totalPages = if (total == 0) 0 else (total / limit + if (total % limit > 0) 1 else 0)
             )
        )
    }
}
