package pl.meleko.trainspot.database.model

import org.jetbrains.exposed.v1.core.ResultRow
import pl.meleko.trainspot.User
import pl.meleko.trainspot.database.UsersTable
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class UserDto(
    val id: Uuid,
    val email: String,
    val username: String,
    val avatarUrl: String,
    val passwordHash: String,
    val bio: String,
    val createdAt: Instant
)

fun UserDto.toUser() = User(
    id = this.id,
    email = this.email,
    username = this.username,
    avatarUrl = this.avatarUrl,
    bio = this.bio,
    createdAt = this.createdAt
)

fun ResultRow.toUserDto() = UserDto(
    id = this[UsersTable.id].value,
    email = this[UsersTable.email],
    username  = this[UsersTable.username],
    bio = this[UsersTable.bio].orEmpty(),
    avatarUrl = this[UsersTable.avatarUrl].orEmpty(),
    passwordHash = this[UsersTable.passwordHash],
    createdAt = this[UsersTable.createdAt]
)

data class UserProfilePictureDto(
    val id: Uuid,
    val avatarUrl: String,
    val createdAt: Instant
)

data class UserStatsDto(
    val totalSpots: Int,
    val totalLikes: Int,
    val followers: Int,
    val following: Int
)
