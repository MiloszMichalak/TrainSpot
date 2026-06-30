package pl.meleko.trainspot.presentation.addspot

import pl.meleko.trainspot.presentation.util.UiText

sealed interface AddSpotEvent {
    object SpotPublished : AddSpotEvent
    data class Error(val message: UiText) : AddSpotEvent
    object NavigateBack : AddSpotEvent
}
