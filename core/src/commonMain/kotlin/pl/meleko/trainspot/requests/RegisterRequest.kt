package pl.meleko.trainspot.requests

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
)