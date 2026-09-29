package pl.meleko.trainspot.presentation.profile

import pl.meleko.trainspot.presentation.util.UiText

sealed interface ProfileEvent {
    data class Error(val message: UiText) : ProfileEvent
    data class Saved(val message: UiText) : ProfileEvent
}
