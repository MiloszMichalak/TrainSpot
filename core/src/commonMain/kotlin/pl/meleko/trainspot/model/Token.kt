package pl.meleko.trainspot.model

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val token: String
)