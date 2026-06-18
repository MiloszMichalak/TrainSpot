package pl.meleko.trainspot.service

import io.ktor.http.HttpStatusCode
import pl.meleko.trainspot.repository.LikesRepository
import pl.meleko.trainspot.util.NetworkResult
import kotlin.uuid.Uuid

object LikesService {
    suspend fun getLikesCount(spotId: Uuid): NetworkResult<Int> {
        val count = LikesRepository.getLikeCountBySpotId(spotId)
        return NetworkResult.Success(count.toInt())
    }

    suspend fun hasLiked(userId: Uuid, spotId: Uuid): NetworkResult<Boolean> {
        val exists = LikesRepository.exists(userId, spotId)
        return NetworkResult.Success(exists)
    }

    suspend fun createLike(userId: Uuid, spotId: Uuid?): NetworkResult<Unit> {
        if (spotId == null) return NetworkResult.Error(HttpStatusCode.BadRequest)

        if (LikesRepository.exists(userId, spotId)) {
            return NetworkResult.Error(HttpStatusCode.Conflict)
        }

        LikesRepository.create(userId, spotId)
        return NetworkResult.Success(Unit)
    }

    suspend fun removeLike(userId: Uuid, spotId: Uuid?): NetworkResult<Unit> {
        if (spotId == null) return NetworkResult.Error(HttpStatusCode.BadRequest)

        val deleted = LikesRepository.delete(userId, spotId)
        return if (deleted) {
            NetworkResult.Success(Unit)
        } else {
            NetworkResult.Error(HttpStatusCode.NotFound)
        }
    }
}