package pl.meleko.trainspot

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val token: String
)