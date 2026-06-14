package pl.meleko.trainspot.requests

data class LoginRequest(
    val emailOrUsername: String,
    val password: String
)