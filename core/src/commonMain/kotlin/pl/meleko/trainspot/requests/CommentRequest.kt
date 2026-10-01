package pl.meleko.trainspot.requests

import kotlinx.serialization.Serializable

@Serializable
data class CommentRequest(val text: String)
