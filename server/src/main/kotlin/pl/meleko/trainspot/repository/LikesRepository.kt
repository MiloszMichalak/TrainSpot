package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import pl.meleko.trainspot.database.LikesTable
import pl.meleko.trainspot.util.dbTransaction
import kotlin.uuid.Uuid

object LikesRepository {
    suspend fun getLikeCountBySpotId(spotId: Uuid): Long {
        return dbTransaction {
            LikesTable.selectAll()
                .where { LikesTable.spotId eq spotId }
                .count()
        }
    }

    suspend fun findByUserId(userId: Uuid): List<Uuid> {
        return dbTransaction {
            LikesTable.selectAll()
                .where { LikesTable.userId eq userId }
                .map { it[LikesTable.id].value }
        }
    }

    suspend fun likesCountFor(spotId: Uuid): Long {
        return dbTransaction {
            LikesTable
                .select(LikesTable.id)
                .where { LikesTable.spotId eq spotId }
                .count()
        }
    }

    suspend fun create(userId: Uuid, spotId: Uuid): Uuid {
        return dbTransaction {
            LikesTable.insert {
                it[LikesTable.userId] = userId
                it[LikesTable.spotId] = spotId
            }[LikesTable.id].value
        }
    }

    suspend fun delete(userId: Uuid, spotId: Uuid): Boolean {
        return dbTransaction {
            LikesTable.deleteWhere {
                (LikesTable.userId eq userId) and (LikesTable.spotId eq spotId)
            } > 0
        }
    }

    suspend fun exists(userId: Uuid, spotId: Uuid): Boolean {
        return dbTransaction {
            LikesTable.selectAll()
                .where { (LikesTable.userId eq userId) and (LikesTable.spotId eq spotId) }
                .count() > 0
        }
    }

    suspend fun findLikedSpotIdsByUserIdPaginated(userId: Uuid, page: Int, limit: Int): List<Uuid> {
        return dbTransaction {
            val offset = page * limit

            LikesTable.selectAll()
                .where { LikesTable.userId eq userId }
                .orderBy(LikesTable.createdAt)
                .offset(offset.toLong())
                .limit(limit)
                .map { it[LikesTable.spotId] }
        }
    }
}