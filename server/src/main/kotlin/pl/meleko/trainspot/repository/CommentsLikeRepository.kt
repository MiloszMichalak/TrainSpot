package pl.meleko.trainspot.repository

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import pl.meleko.trainspot.database.CommentLikesTable
import pl.meleko.trainspot.database.CommentsTable
import pl.meleko.trainspot.util.dbTransaction
import kotlin.uuid.Uuid

enum class CommentLikeCreation { CREATED, ALREADY_EXISTS, COMMENT_NOT_FOUND }

object CommentLikesRepository {
    suspend fun create(commentId: Uuid, userId: Uuid): CommentLikeCreation {
        return dbTransaction {
            if (!CommentsTable.selectAll().where { CommentsTable.id eq commentId }.any()) {
                return@dbTransaction CommentLikeCreation.COMMENT_NOT_FOUND
            }
            if (CommentLikesTable.selectAll().where {
                    (CommentLikesTable.commentId eq commentId) and (CommentLikesTable.userId eq userId)
                }.any()) {
                return@dbTransaction CommentLikeCreation.ALREADY_EXISTS
            }
            CommentLikesTable.insert {
                it[CommentLikesTable.commentId] = commentId
                it[CommentLikesTable.userId] = userId
            }
            CommentLikeCreation.CREATED
        }
    }

    suspend fun delete(commentId: Uuid, userId: Uuid): Boolean = dbTransaction {
        CommentLikesTable.deleteWhere {
            (CommentLikesTable.commentId eq commentId) and (CommentLikesTable.userId eq userId)
        } > 0
    }
}