package pl.meleko.trainspot.presentation.register

import pl.meleko.trainspot.presentation.util.UiText

sealed interface RegisterEvent {
    data object RegisterSuccess: RegisterEvent
    data class Error(val error: UiText): RegisterEvent
}
