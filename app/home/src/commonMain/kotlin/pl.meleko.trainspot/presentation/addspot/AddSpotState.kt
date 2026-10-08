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
    val locationRequestId: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val trainSuggestions: List<TrainSuggestion> = emptyList(),
    val selectedTrainSuggestion: TrainSuggestion? = null,
    val trainNumber: String = "",
    val vehicleNumber: String = "",
    val isManualTrain: Boolean = false,
    val showManualTrainRoute: Boolean = false,
    val originStationQuery: String = "",
    val destinationStationQuery: String = "",
    val originStation: Station? = null,
    val destinationStation: Station? = null,
    val originStationSuggestions: List<Station> = emptyList(),
    val destinationStationSuggestions: List<Station> = emptyList(),
    val rollingStockModel: String = "",
    val rollingStockCarrierCode: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val isPublishing: Boolean = false,
    val isEditMode: Boolean = false
) {
    val isLocatingStation: Boolean
        get() = locationRequestId != null

    val canPublish: Boolean
        get() = !isLoading && !isPublishing && !isLocatingStation &&
            (selectedMedia != null || (isEditMode && initialImageUrl != null)) &&
            rollingStockModel.isNotBlank() &&
            isTrainInputValid

    val isTrainInputValid: Boolean
        get() = !isManualTrain || (
            (originStationQuery.isBlank() && destinationStationQuery.isBlank() ||
                originStation != null && destinationStation != null) &&
                (trainNumber.isNotBlank() ||
                    originStationQuery.isBlank() && destinationStationQuery.isBlank()) &&
                (trainNumber.isBlank() || selectedStation != null) &&
                trainNumber.trim().length <= 50
            )
}

data class SelectedSpotMedia(
    val file: PlatformFile,
    val fileName: String,
    val contentType: String?,
)

data class TrainSuggestion(
    val trainOrderId: Int,
    val number: String,
    val category: String,
    val time: String,
    val stationName: String,
    val carrierCode: String,
    val trainName: String
)
