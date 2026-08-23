package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import pl.meleko.trainspot.database.LikesTable
import pl.meleko.trainspot.database.entities.LikeEntity
import pl.meleko.trainspot.database.entities.SpotEntity
import pl.meleko.trainspot.database.entities.UserEntity
import pl.meleko.trainspot.database.model.LikeDto
import pl.meleko.trainspot.util.dbTransaction
import kotlin.uuid.Uuid

object LikesRepository {
    suspend fun getLikeCountBySpotId(spotId: Uuid): Long {
        return dbTransaction {
            LikeEntity.find { LikesTable.spotId eq spotId }.count()
        }
    }

    suspend fun findByUserId(userId: Uuid): List<LikeDto> {
        return dbTransaction {
            LikeEntity.find { LikesTable.userId eq userId }.map { it.toDto() }
        }
    }

    suspend fun likesCountFor(spotId: Uuid): Long {
        return dbTransaction {
            LikeEntity.find { LikesTable.spotId eq spotId }.count()
        }
    }

    suspend fun create(userId: Uuid, spotId: Uuid): Uuid {
        return dbTransaction {
            val user = UserEntity.findById(userId) ?: throw IllegalArgumentException("User not found")
            val spot = SpotEntity.findById(spotId) ?: throw IllegalArgumentException("Spot not found")

            LikeEntity.new {
                this.user = user
                this.spot = spot
            }.id.value
        }
    }

    suspend fun delete(userId: Uuid, spotId: Uuid): Boolean {
        return dbTransaction {
            LikeEntity.find { (LikesTable.userId eq userId) and (LikesTable.spotId eq spotId) }
                .firstOrNull()?.delete()
            true
        }
    }

    suspend fun exists(userId: Uuid, spotId: Uuid): Boolean {
        return dbTransaction {
            LikeEntity.find { (LikesTable.userId eq userId) and (LikesTable.spotId eq spotId) }.count() > 0
        }
    }

    suspend fun countByUserId(userId: Uuid): Long {
        return dbTransaction {
            LikeEntity.find { LikesTable.userId eq userId }.count()
        }
    }

    suspend fun findLikedSpotIdsByUserIdPaginated(userId: Uuid, page: Int, limit: Int): List<Uuid> {
        return dbTransaction {
            val offset = (page * limit).toLong()

            LikeEntity.find { LikesTable.userId eq userId }
                .orderBy(LikesTable.createdAt to SortOrder.ASC)
                .limit(limit)
                .offset(offset)
                .map { it.spot.id.value }
        }
    }
}