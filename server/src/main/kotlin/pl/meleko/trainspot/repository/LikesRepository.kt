package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import pl.meleko.trainspot.database.LikesTable
import pl.meleko.trainspot.database.SpotsTable
import pl.meleko.trainspot.database.UsersTable
import pl.meleko.trainspot.util.dbTransaction
import kotlin.uuid.Uuid

object LikesRepository {
    suspend fun getLikeCountBySpotId(spotId: Uuid): Long = dbTransaction {
        LikesTable
            .selectAll()
            .where { LikesTable.spotId eq spotId }
            .count()
    }

    suspend fun likesCountFor(spotId: Uuid): Long = getLikeCountBySpotId(spotId)

    suspend fun create(userId: Uuid, spotId: Uuid): Uuid = dbTransaction {
        if (!UsersTable
            .selectAll()
            .where { UsersTable.id eq userId }
            .any()) {
            throw IllegalArgumentException("User not found")
        }

        if (!SpotsTable
            .selectAll()
            .where { SpotsTable.id eq spotId }
            .any()) {
            throw IllegalArgumentException("Spot not found")
        }

        LikesTable.insertAndGetId {
            it[LikesTable.userId] = userId
            it[LikesTable.spotId] = spotId
        }.value
    }

    suspend fun delete(userId: Uuid, spotId: Uuid): Boolean = dbTransaction {
        LikesTable
            .deleteWhere { (LikesTable.userId eq userId) and (LikesTable.spotId eq spotId) } > 0
    }

    suspend fun exists(userId: Uuid, spotId: Uuid): Boolean = dbTransaction {
        LikesTable
            .selectAll()
            .where { (LikesTable.userId eq userId) and (LikesTable.spotId eq spotId) }
            .any()
    }

    suspend fun countByUserId(userId: Uuid): Long = dbTransaction {
        LikesTable
            .selectAll()
            .where { LikesTable.userId eq userId }
            .count()
    }

    suspend fun findLikedSpotIdsByUserIdPaginated(userId: Uuid, page: Int, limit: Int): List<Uuid> = dbTransaction {
        LikesTable.selectAll()
            .where { LikesTable.userId eq userId }
            .orderBy(LikesTable.createdAt to SortOrder.ASC)
            .limit(limit)
            .offset((page * limit).toLong())
            .map { it[LikesTable.spotId].value }
    }
}
