package pl.meleko.trainspot.presentation.addspot

import pl.meleko.trainspot.model.Station

sealed interface AddSpotAction {
    data class OnImagePicked(val uri: String, val bytes: ByteArray) : AddSpotAction
    data class OnStationSearchQueryChanged(val query: String) : AddSpotAction
    data class OnStationSelected(val station: Station) : AddSpotAction
    data class OnTrainSelected(val train: TrainSuggestion) : AddSpotAction
    data class OnTrainNumberChanged(val number: String) : AddSpotAction
    data class OnRollingStockModelChanged(val model: String) : AddSpotAction
    data class OnDescriptionChanged(val description: String) : AddSpotAction
    object OnPublishClick : AddSpotAction
    object OnBackClick : AddSpotAction
}
