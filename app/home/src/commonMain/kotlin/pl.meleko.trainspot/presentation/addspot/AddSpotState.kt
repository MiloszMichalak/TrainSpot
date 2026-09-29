package pl.meleko.trainspot.presentation.addspot

import io.github.vinceglb.filekit.PlatformFile
import pl.meleko.trainspot.model.Station

data class AddSpotState(
    val spotId: String? = null,
    val selectedMedia: SelectedSpotMedia? = null,
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
) {
    val canPublish: Boolean
        get() = !isPublishing &&
            (selectedMedia != null || (isEditMode && initialImageUrl != null)) &&
            (isEditMode || selectedTrainSuggestion != null)
}

data class SelectedSpotMedia(
    val file: PlatformFile,
    val fileName: String,
    val contentType: String?,
)

data class TrainSuggestion(
    val scheduleId: Int,
    val number: String,
    val category: String,
    val time: String,
    val stationName: String,
    val carrierCode: String,
    val trainName: String
)
