package pl.meleko.trainspot.presentation.comments

import pl.meleko.trainspot.presentation.util.UiText

sealed interface CommentsEvent {
    data class Error(val text: UiText) : CommentsEvent
}
