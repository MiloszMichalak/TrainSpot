package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import pl.meleko.trainspot.database.LikesTable
import kotlin.uuid.Uuid

object LikesRepository {
    fun getLikeCountBySpotId(spotId: Uuid): Long = transaction {
        LikesTable.selectAll()
            .where { LikesTable.spotId eq spotId }
            .count()
    }

    fun findByUserId(userId: Uuid): List<Uuid> = transaction {
        LikesTable.selectAll()
            .where { LikesTable.userId eq userId }
            .map { it[LikesTable.id].value }
    }

    fun likesCountFor(spotId: Uuid): Long = transaction {
        LikesTable
            .select(LikesTable.id)
            .where { LikesTable.spotId eq spotId }
            .count()
    }

    fun create(userId: Uuid, spotId: Uuid): Uuid = transaction {
        LikesTable.insert {
            it[LikesTable.userId] = userId
            it[LikesTable.spotId] = spotId
        }[LikesTable.id].value
    }

    fun delete(userId: Uuid, spotId: Uuid): Boolean = transaction {
        LikesTable.deleteWhere {
            (LikesTable.userId eq userId) and (LikesTable.spotId eq spotId)
        } > 0
    }

    fun exists(userId: Uuid, spotId: Uuid): Boolean = transaction {
        LikesTable.selectAll()
            .where { (LikesTable.userId eq userId) and (LikesTable.spotId eq spotId) }
            .count() > 0
    }
}