package pl.meleko.trainspot.presentation.feed

import pl.meleko.trainspot.presentation.util.UiText

sealed interface FeedEvent {
    data class Error(val error: UiText) : FeedEvent
}
