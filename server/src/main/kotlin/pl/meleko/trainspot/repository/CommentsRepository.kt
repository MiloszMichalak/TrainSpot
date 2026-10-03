package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import pl.meleko.trainspot.database.CommentLikesTable
import pl.meleko.trainspot.database.CommentsTable
import pl.meleko.trainspot.database.SpotsTable
import pl.meleko.trainspot.database.UsersTable
import pl.meleko.trainspot.model.Comment
import pl.meleko.trainspot.model.CommentAuthor
import pl.meleko.trainspot.response.PaginationResponse
import pl.meleko.trainspot.util.dbTransaction
import pl.meleko.trainspot.util.toKotlinInstantOrNull
import pl.meleko.trainspot.util.toRequiredKotlinInstant
import kotlin.math.ceil
import kotlin.uuid.Uuid

object CommentsRepository {
    suspend fun spotExists(spotId: Uuid): Boolean = dbTransaction {
        SpotsTable.selectAll().where { SpotsTable.id eq spotId }.any()
    }

    suspend fun findBySpot(spotId: Uuid, viewerId: Uuid, page: Int, limit: Int): PaginationResponse<Comment> = dbTransaction {
        val condition = CommentsTable.spotId eq spotId
        val total = CommentsTable.selectAll().where { condition }.count().toInt()
        val rows = CommentsTable.selectAll()
            .where { condition }
            .orderBy(CommentsTable.createdAt to SortOrder.DESC, CommentsTable.id to SortOrder.DESC)
            .limit(limit)
            .offset(page.toLong() * limit)
            .toList()

        PaginationResponse(
            items = mapComments(rows, viewerId),
            total = total,
            page = page,
            limit = limit,
            totalPages = ceil(total.toDouble() / limit).toInt()
        )
    }

    suspend fun findById(commentId: Uuid, viewerId: Uuid, spotId: Uuid? = null): Comment? = dbTransaction {
        val condition = if (spotId == null) {
            CommentsTable.id eq commentId
        } else {
            (CommentsTable.id eq commentId) and (CommentsTable.spotId eq spotId)
        }
        val rows = CommentsTable.selectAll().where { condition }.toList()
        mapComments(rows, viewerId).singleOrNull()
    }

    suspend fun create(spotId: Uuid, userId: Uuid, text: String, commentId: Uuid): Comment? = dbTransaction {
        if (!SpotsTable.selectAll().where { SpotsTable.id eq spotId }.any()) return@dbTransaction null
        CommentsTable.insert {
            it[CommentsTable.id] = commentId
            it[CommentsTable.spotId] = spotId
            it[CommentsTable.userId] = userId
            it[CommentsTable.text] = text
        }
        val rows = CommentsTable.selectAll().where { CommentsTable.id eq commentId }.toList()
        mapComments(rows, userId).singleOrNull()
    }

    suspend fun update(commentId: Uuid, userId: Uuid, text: String): Boolean = dbTransaction {
        CommentsTable.update({ (CommentsTable.id eq commentId) and (CommentsTable.userId eq userId) }) {
            it[CommentsTable.text] = text
            it[CommentsTable.updatedAt] = java.time.OffsetDateTime.now()
        } > 0
    }

    suspend fun delete(commentId: Uuid, userId: Uuid): Boolean = dbTransaction {
        CommentsTable.deleteWhere { (CommentsTable.id eq commentId) and (CommentsTable.userId eq userId) } > 0
    }

    private fun mapComments(rows: List<ResultRow>, viewerId: Uuid): List<Comment> {
        if (rows.isEmpty()) return emptyList()

        val commentIds = rows.map { it[CommentsTable.id].value }
        val authorIds = rows.map { it[CommentsTable.userId].value }.distinct()
        val authors = UsersTable.selectAll()
            .where { UsersTable.id inList authorIds }
            .associate { row ->
                row[UsersTable.id].value to CommentAuthor(
                    id = row[UsersTable.id].value,
                    username = row[UsersTable.username],
                    avatarUrl = row[UsersTable.avatarUrl]
                )
            }
        val likesByComment = CommentLikesTable.selectAll()
            .where { CommentLikesTable.commentId inList commentIds }
            .groupBy { it[CommentLikesTable.commentId].value }

        return rows.mapNotNull { row ->
            val commentId = row[CommentsTable.id].value
            val author = authors[row[CommentsTable.userId].value] ?: return@mapNotNull null
            val likes = likesByComment[commentId].orEmpty()
            Comment(
                id = commentId,
                spotId = row[CommentsTable.spotId].value,
                author = author,
                text = row[CommentsTable.text],
                createdAt = row[CommentsTable.createdAt].toRequiredKotlinInstant(),
                updatedAt = row[CommentsTable.updatedAt].toKotlinInstantOrNull(),
                likesCount = likes.size.toLong(),
                isLiked = likes.any { it[CommentLikesTable.userId].value == viewerId }
            )
        }
    }
}
