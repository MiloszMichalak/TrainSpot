package pl.meleko.trainspot.requests

data class UpdateProfileRequest(
    val email: String? = null,
    val username: String? = null,
    val bio: String? = null
)