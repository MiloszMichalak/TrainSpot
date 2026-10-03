package pl.meleko.trainspot.presentation.feed

import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.model.User
import pl.meleko.trainspot.presentation.util.UiText

data class FeedState(
    val spots: List<Spot> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
    val currentUser: User? = null,
    val pendingLikeSpotIds: Set<String> = emptySet()
)
