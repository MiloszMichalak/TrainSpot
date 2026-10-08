package pl.meleko.trainspot.presentation.addspot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.meleko.trainspot.core.onFailure
import pl.meleko.trainspot.core.onSuccess
import pl.meleko.trainspot.domain.DictionaryRepository
import pl.meleko.trainspot.domain.ScheduleRepository
import pl.meleko.trainspot.domain.SpotRepository
import pl.meleko.trainspot.model.ScheduleRoute
import pl.meleko.trainspot.model.Station
import pl.meleko.trainspot.model.TrainModel
import pl.meleko.trainspot.presentation.util.UiText
import pl.meleko.trainspot.presentation.util.toUiText
import pl.meleko.trainspot.requests.SpotRequest
import trainspot.app.shared.generated.resources.Res
import trainspot.app.shared.generated.resources.error_invalid_data
import trainspot.app.shared.generated.resources.error_location_unavailable
import trainspot.app.shared.generated.resources.error_media_file
import trainspot.app.shared.generated.resources.error_no_nearby_station

class AddSpotViewModel(
    private val spotId: String? = null,
    private val spotRepository: SpotRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddSpotState(spotId = spotId, isEditMode = spotId != null))
    val state = _state.asStateFlow()

    private val _events = Channel<AddSpotEvent>()
    val events = _events.receiveAsFlow()

    private var locationRequestCounter = 0
    private var stationSearchJob: Job? = null
    private var stationLocationJob: Job? = null
    private var trainSearchJob: Job? = null

    init {
        if (spotId != null) {
            loadSpotDetails(spotId)
        }
    }

    private fun loadSpotDetails(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            spotRepository.getSpot(id)
                .onSuccess { spot ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            initialImageUrl = spot.imageUrl,
                            selectedTrainSuggestion = spot.trainRun?.toTrainSuggestion(),
                            vehicleNumber = spot.model.number.orEmpty(),
                            rollingStockModel = spot.model.model,
                            rollingStockCarrierCode = spot.model.carrierCode.orEmpty(),
                            trainNumber = spot.trainRun?.nationalNumber.orEmpty(),
                            isManualTrain = (spot.trainRun?.trainOrderId ?: 0) < 0,
                            showManualTrainRoute = (spot.trainRun?.trainOrderId ?: 0) < 0,
                            originStation = spot.trainRun?.originStation,
                            originStationQuery = spot.trainRun?.originStation?.name.orEmpty(),
                            destinationStation = spot.trainRun?.destStation,
                            destinationStationQuery = spot.trainRun?.destStation?.name.orEmpty(),
                            description = spot.description,
                            selectedStation = spot.station,
                            latitude = spot.lat,
                            longitude = spot.lon,
                            stationSearchQuery = spot.station?.name ?: ""
                        )
                    }
                    spot.station?.id?.let { loadTrainsForStation(it) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    _events.send(AddSpotEvent.Error(error.toUiText()))
                }
        }
    }

    fun onAction(action: AddSpotAction) {
        when (action) {
            is AddSpotAction.OnMediaPicked -> {
                _state.update { it.copy(selectedMedia = action.media) }
            }
            AddSpotAction.OnMediaPickFailed -> {
                viewModelScope.launch {
                    _events.send(AddSpotEvent.Error(UiText.ResString(Res.string.error_media_file)))
                }
            }
            is AddSpotAction.OnStationSearchQueryChanged -> {
                cancelStationLocation()
                trainSearchJob?.cancel()
                _state.update {
                    it.copy(
                        stationSearchQuery = action.query,
                        selectedStation = null,
                        selectedTrainSuggestion = null,
                        trainNumber = if (it.isManualTrain) it.trainNumber else "",
                        trainSuggestions = emptyList()
                    )
                }
                searchStations(action.query)
            }
            is AddSpotAction.OnStationSelected -> {
                selectStation(action.station)
            }
            AddSpotAction.OnLocateStationClick -> {
                if (!state.value.isLocatingStation && !state.value.isLoading && !state.value.isPublishing) {
                    val requestId = ++locationRequestCounter
                    _state.update { it.copy(locationRequestId = requestId) }
                }
            }
            is AddSpotAction.OnLocationResolved -> {
                findStationForLocation(action)
            }
            is AddSpotAction.OnLocationFailed -> {
                if (state.value.locationRequestId == action.requestId) {
                    cancelStationLocation()
                    viewModelScope.launch { _events.send(AddSpotEvent.Error(action.message)) }
                }
            }
            is AddSpotAction.OnLocationCancelled -> {
                if (state.value.locationRequestId == action.requestId) cancelStationLocation()
            }
            is AddSpotAction.OnTrainSelected -> {
                _state.update {
                    it.copy(
                        selectedTrainSuggestion = action.train,
                        trainNumber = action.train.number,
                        isManualTrain = false,
                        showManualTrainRoute = false,
                        originStation = null,
                        originStationQuery = "",
                        destinationStation = null,
                        destinationStationQuery = "",
                        originStationSuggestions = emptyList(),
                        destinationStationSuggestions = emptyList(),
                        rollingStockCarrierCode = action.train.carrierCode
                    )
                }
            }
            is AddSpotAction.OnTrainNumberChanged -> {
                _state.update {
                    it.copy(trainNumber = action.number, isManualTrain = true, selectedTrainSuggestion = null)
                }
            }
            AddSpotAction.OnManualTrainClick -> {
                _state.update { it.copy(showManualTrainRoute = !it.showManualTrainRoute) }
            }
            AddSpotAction.OnClearTrainClick -> {
                _state.update {
                    it.copy(
                        isManualTrain = false,
                        showManualTrainRoute = false,
                        selectedTrainSuggestion = null,
                        trainNumber = "",
                        originStation = null,
                        originStationQuery = "",
                        destinationStation = null,
                        destinationStationQuery = "",
                        originStationSuggestions = emptyList(),
                        destinationStationSuggestions = emptyList()
                    )
                }
            }
            is AddSpotAction.OnVehicleNumberChanged -> {
                _state.update { it.copy(vehicleNumber = action.number) }
            }
            is AddSpotAction.OnRouteStationQueryChanged -> {
                _state.update {
                    if (action.isOrigin) it.copy(originStationQuery = action.query, originStation = null, originStationSuggestions = emptyList(), isManualTrain = true, selectedTrainSuggestion = null)
                    else it.copy(destinationStationQuery = action.query, destinationStation = null, destinationStationSuggestions = emptyList(), isManualTrain = true, selectedTrainSuggestion = null)
                }
                searchRouteStations(action.query, action.isOrigin)
            }
            is AddSpotAction.OnRouteStationSelected -> {
                _state.update {
                    if (action.isOrigin) it.copy(originStationQuery = action.station.name, originStation = action.station, originStationSuggestions = emptyList())
                    else it.copy(destinationStationQuery = action.station.name, destinationStation = action.station, destinationStationSuggestions = emptyList())
                }
            }
            is AddSpotAction.OnRollingStockModelChanged -> {
                _state.update { it.copy(rollingStockModel = action.model) }
            }
            is AddSpotAction.OnDescriptionChanged -> {
                _state.update { it.copy(description = action.description) }
            }
            AddSpotAction.OnPublishClick -> {
                if (state.value.isEditMode) {
                    updateSpot()
                } else {
                    publishSpot()
                }
            }
            AddSpotAction.OnBackClick -> {
                viewModelScope.launch {
                    _events.send(AddSpotEvent.NavigateBack)
                }
            }
        }
    }

    private fun cancelStationLocation() {
        stationLocationJob?.cancel()
        _state.update { it.copy(locationRequestId = null) }
    }

    private fun selectStation(station: Station) {
        stationSearchJob?.cancel()
        cancelStationLocation()
        _state.update {
            it.copy(
                selectedStation = station,
                stationSearchQuery = station.name,
                stationSuggestions = emptyList(),
                selectedTrainSuggestion = null,
                trainNumber = if (it.isManualTrain) it.trainNumber else "",
                trainSuggestions = emptyList()
            )
        }
        loadTrainsForStation(station.id)
    }

    private fun findStationForLocation(action: AddSpotAction.OnLocationResolved) {
        if (state.value.locationRequestId != action.requestId) return
        stationLocationJob?.cancel()
        stationLocationJob = viewModelScope.launch {
            try {
                _state.update { it.copy(latitude = action.latitude, longitude = action.longitude) }

                val station = dictionaryRepository.findNearestStation(
                    latitude = action.latitude,
                    longitude = action.longitude,
                    maxDistanceMeters = 5_000.0
                )

                if (state.value.locationRequestId != action.requestId) return@launch

                if (station == null) {
                    _state.update { it.copy(locationRequestId = null) }
                    _events.send(AddSpotEvent.Error(UiText.ResString(Res.string.error_no_nearby_station)))
                } else {
                    stationLocationJob = null
                    selectStation(station)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                if (state.value.locationRequestId == action.requestId) {
                    _state.update { it.copy(locationRequestId = null) }
                    _events.send(AddSpotEvent.Error(UiText.ResString(Res.string.error_location_unavailable)))
                }
            }
        }
    }

    private fun searchStations(query: String) {
        stationSearchJob?.cancel()
        if (query.length < 2) {
            _state.update { it.copy(stationSuggestions = emptyList()) }
            return
        }
        stationSearchJob = viewModelScope.launch {
            val results = dictionaryRepository.searchStations(query.trim())
            if (state.value.stationSearchQuery == query && state.value.selectedStation == null) {
                val exactMatches = results.filter { it.name.equals(query.trim(), ignoreCase = true) }
                if (exactMatches.size == 1) {
                    stationSearchJob = null
                    selectStation(exactMatches.single())
                } else {
                    _state.update { it.copy(stationSuggestions = results) }
                }
            }
        }
    }

    private fun loadTrainsForStation(stationId: Int) {
        trainSearchJob?.cancel()
        trainSearchJob = viewModelScope.launch {
            scheduleRepository.getRecentTrains(stationId)
                .onSuccess { routes ->
                    if (state.value.selectedStation?.id != stationId) return@launch
                    _state.update { state ->
                        state.copy(
                            trainSuggestions = routes.map { it.toTrainSuggestion() },
                        )
                    }
                }
                .onFailure { error ->
                    if (state.value.selectedStation?.id == stationId) {
                        _events.send(AddSpotEvent.Error(error.toUiText()))
                    }
                }
        }
    }

    private var originStationSearchJob: Job? = null
    private var destinationStationSearchJob: Job? = null

    private fun searchRouteStations(query: String, isOrigin: Boolean) {
        if (isOrigin) originStationSearchJob?.cancel() else destinationStationSearchJob?.cancel()
        if (query.length < 2) return
        val job = viewModelScope.launch {
            val results = dictionaryRepository.searchStations(query)
            _state.update {
                when {
                    isOrigin && it.originStationQuery == query && it.originStation == null -> it.copy(originStationSuggestions = results)
                    !isOrigin && it.destinationStationQuery == query && it.destinationStation == null -> it.copy(destinationStationSuggestions = results)
                    else -> it
                }
            }
        }
        if (isOrigin) originStationSearchJob = job else destinationStationSearchJob = job
    }

    private fun publishSpot() {
        val currentState = state.value
        val selectedMedia = currentState.selectedMedia
        if (selectedMedia == null || !currentState.canPublish) {
            viewModelScope.launch {
                _events.send(AddSpotEvent.Error(UiText.ResString(Res.string.error_invalid_data)))
            }
            return
        }

        viewModelScope.launch {
            if (state.value.isPublishing) return@launch
            _state.update { it.copy(isPublishing = true) }
            try {
                // Read the file before creating the database row so file access failures
                // cannot leave an orphaned spot.
                val imageBytes = selectedMedia.file.readBytes()
                val existingDraftId = currentState.spotId
                // A retry may follow a change of train; save the latest form before the image.
                if (existingDraftId != null) {
                    when (val update = spotRepository.updateSpot(existingDraftId, currentState.toSpotRequest())) {
                        is pl.meleko.trainspot.core.Result.Success -> Unit
                        is pl.meleko.trainspot.core.Result.Error -> {
                            _events.send(AddSpotEvent.Error(update.error.toUiText()))
                            return@launch
                        }
                    }
                }
                val draftId = existingDraftId
                    ?: when (val creation = spotRepository.createSpot(currentState.toSpotRequest())) {
                        is pl.meleko.trainspot.core.Result.Success -> {
                            creation.data.id.toString().also { id ->
                                _state.update { it.copy(spotId = id) }
                            }
                        }

                        is pl.meleko.trainspot.core.Result.Error -> {
                            _events.send(AddSpotEvent.Error(creation.error.toUiText()))
                            return@launch
                        }
                    }

                when (val upload = spotRepository.updateSpotImage(
                    id = draftId,
                    image = imageBytes,
                    fileName = selectedMedia.fileName,
                    mimeType = selectedMedia.contentType ?: "image/jpeg"
                )) {
                    is pl.meleko.trainspot.core.Result.Success -> {
                        _state.update { it.copy(spotId = null) }
                        _events.send(AddSpotEvent.SpotPublished)
                    }
                    is pl.meleko.trainspot.core.Result.Error -> {
                        _events.send(AddSpotEvent.Error(upload.error.toUiText()))
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                _events.send(AddSpotEvent.Error(UiText.ResString(Res.string.error_media_file)))
            } finally {
                _state.update { it.copy(isPublishing = false) }
            }
        }
    }

    private fun updateSpot() {
        val id = spotId ?: return
        if (!state.value.canPublish) return
        
        viewModelScope.launch {
            _state.update { it.copy(isPublishing = true) }
            try {
                val currentState = state.value
                spotRepository.updateSpot(
                    id = id,
                    request = currentState.toSpotRequest()
                )
                    .onSuccess { _events.send(AddSpotEvent.SpotPublished) }
                    .onFailure { error ->
                        _events.send(AddSpotEvent.Error(error.toUiText()))
                    }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                _events.send(AddSpotEvent.Error(UiText.ResString(Res.string.error_media_file)))
            } finally {
                _state.update { it.copy(isPublishing = false) }
            }
        }
    }

    private fun AddSpotState.toSpotRequest() = SpotRequest(
        trainModel = TrainModel(
            model = rollingStockModel,
            number = vehicleNumber.trim().takeIf(String::isNotEmpty),
            carrierCode = rollingStockCarrierCode.trim().takeIf { !isManualTrain && it.isNotEmpty() }
        ),
        stationId = selectedStation?.id,
        trainRunId = selectedTrainSuggestion?.trainOrderId.takeUnless { isManualTrain },
        manualTrainNumber = trainNumber.trim().takeIf { isManualTrain && it.isNotEmpty() },
        originStationId = originStation?.id.takeIf { isManualTrain },
        destinationStationId = destinationStation?.id.takeIf { isManualTrain },
        description = description,
        lat = latitude ?: selectedStation?.latitude,
        lon = longitude ?: selectedStation?.longitude
    )

    private fun ScheduleRoute.toTrainSuggestion() = TrainSuggestion(
        trainOrderId = trainOrderId,
        number = nationalNumber ?: trainOrderId.toString(),
        category = commercialCategorySymbol,
        time = departureTime ?: arrivalTime ?: "",
        stationName = destStation?.name ?: originStation?.name ?: "",
        carrierCode = carrierCode,
        trainName = name.orEmpty()
    )
}
