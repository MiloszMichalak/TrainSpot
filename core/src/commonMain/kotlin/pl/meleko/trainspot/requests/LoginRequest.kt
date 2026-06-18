package pl.meleko.trainspot.requests

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val emailOrUsername: String,
    val password: String
)