package pl.meleko.trainspot.presentation.addspot

import pl.meleko.trainspot.model.Station
import pl.meleko.trainspot.presentation.util.UiText

sealed interface AddSpotAction {
    data class OnMediaPicked(val media: SelectedSpotMedia) : AddSpotAction
    data object OnMediaPickFailed : AddSpotAction
    data class OnStationSearchQueryChanged(val query: String) : AddSpotAction
    data class OnStationSelected(val station: Station) : AddSpotAction
    data object OnLocateStationClick : AddSpotAction
    data class OnLocationResolved(val requestId: Int, val latitude: Double, val longitude: Double) : AddSpotAction
    data class OnLocationFailed(val requestId: Int, val message: UiText) : AddSpotAction
    data class OnLocationCancelled(val requestId: Int) : AddSpotAction
    data class OnTrainSelected(val train: TrainSuggestion) : AddSpotAction
    data class OnTrainNumberChanged(val number: String) : AddSpotAction
    data class OnRollingStockModelChanged(val model: String) : AddSpotAction
    data class OnDescriptionChanged(val description: String) : AddSpotAction
    data object OnPublishClick : AddSpotAction
    data object OnBackClick : AddSpotAction
}
