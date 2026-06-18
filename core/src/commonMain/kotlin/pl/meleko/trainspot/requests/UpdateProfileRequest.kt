package pl.meleko.trainspot.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val email: String? = null,
    val username: String? = null,
    val bio: String? = null
)