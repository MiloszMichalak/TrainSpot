package pl.meleko.trainspot.response

import kotlinx.serialization.Serializable
import pl.meleko.trainspot.model.User

@Serializable
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val user: User
)