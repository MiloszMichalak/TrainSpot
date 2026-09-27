package pl.meleko.trainspot.presentation.login

import pl.meleko.trainspot.presentation.util.UiText

sealed interface LoginEvent {
    data object LoginSuccess: LoginEvent
    data class Error(val error: UiText): LoginEvent
}
