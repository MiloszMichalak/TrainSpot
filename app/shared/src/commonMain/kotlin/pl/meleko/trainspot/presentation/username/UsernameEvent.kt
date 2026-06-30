package pl.meleko.trainspot.presentation.username

import pl.meleko.trainspot.presentation.util.UiText

sealed interface UsernameEvent {
    object Success : UsernameEvent
    data class Error(val message: UiText) : UsernameEvent
}
