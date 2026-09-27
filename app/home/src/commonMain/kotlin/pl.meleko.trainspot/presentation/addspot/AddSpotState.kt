package pl.meleko.trainspot.presentation.addspot

import pl.meleko.trainspot.model.Station

data class AddSpotState(
    val spotId: String? = null,
    val selectedImageUri: String? = null,
    val selectedImageBytes: ByteArray? = null,
    val initialImageUrl: String? = null,
    val stationSearchQuery: String = "",
    val stationSuggestions: List<Station> = emptyList(),
    val selectedStation: Station? = null,
    val trainSuggestions: List<TrainSuggestion> = emptyList(),
    val selectedTrainSuggestion: TrainSuggestion? = null,
    val trainNumber: String = "",
    val rollingStockModel: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val isPublishing: Boolean = false,
    val isEditMode: Boolean = false
)

data class TrainSuggestion(
    val number: String,
    val category: String,
    val time: String,
    val stationName: String,
    val carrierCode: String,
    val trainName: String
)
