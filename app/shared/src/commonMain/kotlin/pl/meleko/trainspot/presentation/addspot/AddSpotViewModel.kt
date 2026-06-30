package pl.meleko.trainspot.presentation.addspot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import pl.meleko.trainspot.core.onFailure
import pl.meleko.trainspot.core.onSuccess
import pl.meleko.trainspot.domain.repository.DictionaryRepository
import pl.meleko.trainspot.domain.repository.ScheduleRepository
import pl.meleko.trainspot.domain.repository.SpotRepository
import pl.meleko.trainspot.model.TrainModel
import pl.meleko.trainspot.presentation.util.toUiText
import pl.meleko.trainspot.requests.SpotRequest

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
                            trainNumber = spot.model.number,
                            rollingStockModel = spot.model.model,
                            description = spot.description,
                            selectedStation = spot.station,
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
            is AddSpotAction.OnImagePicked -> {
                _state.update { it.copy(selectedImageUri = action.uri, selectedImageBytes = action.bytes) }
            }
            is AddSpotAction.OnStationSearchQueryChanged -> {
                _state.update { it.copy(stationSearchQuery = action.query) }
                searchStations(action.query)
            }
            is AddSpotAction.OnStationSelected -> {
                _state.update {
                    it.copy(
                        selectedStation = action.station,
                        stationSearchQuery = action.station.name,
                        stationSuggestions = emptyList()
                    )
                }
                loadTrainsForStation(action.station.id)
            }
            is AddSpotAction.OnTrainSelected -> {
                _state.update {
                    it.copy(
                        selectedTrainSuggestion = action.train,
                        trainNumber = action.train.number,
                        rollingStockModel = action.train.model
                    )
                }
            }
            is AddSpotAction.OnTrainNumberChanged -> {
                _state.update { it.copy(trainNumber = action.number) }
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

    private fun searchStations(query: String) {
        if (query.length < 2) {
            _state.update { it.copy(stationSuggestions = emptyList()) }
            return
        }
        viewModelScope.launch {
            val results = dictionaryRepository.searchStations(query)
            _state.update { it.copy(stationSuggestions = results) }
        }
    }

    private fun loadTrainsForStation(stationId: Int) {
        viewModelScope.launch {
            scheduleRepository.getRecentTrains(stationId)
                .onSuccess { routes ->
                    _state.update {
                        it.copy(
                            trainSuggestions = routes.map { route ->
                                TrainSuggestion(
                                    number = route.nationalNumber ?: route.trainOrderId.toString(),
                                    model = route.commercialCategorySymbol,
                                    time = route.stations.find { s -> s.stationId.id == stationId }?.departureTime ?: "",
                                    destination = route.stations.lastOrNull()?.stationId?.name ?: "",
                                    carrierCode = route.carrierCode
                                )
                            }
                        )
                    }
                }
        }
    }

    private fun publishSpot() {
        val imageBytes = state.value.selectedImageBytes ?: return // Should show error if no image
        
        viewModelScope.launch {
            _state.update { it.copy(isPublishing = true) }
            
            val request = SpotRequest(
                trainModel = TrainModel(
                    model = state.value.rollingStockModel,
                    number = state.value.trainNumber,
                    carrierCode = state.value.selectedTrainSuggestion?.carrierCode ?: ""
                ),
                stationId = state.value.selectedStation?.id,
                trainRunId = null,
                description = state.value.description,
                lat = null,
                lon = null
            )

            spotRepository.createSpot(request, imageBytes)
                .onSuccess {
                    _events.send(AddSpotEvent.SpotPublished)
                }
                .onFailure { error ->
                    _events.send(AddSpotEvent.Error(error.toUiText()))
                }
            
            _state.update { it.copy(isPublishing = false) }
        }
    }

    private fun updateSpot() {
        val id = spotId ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isPublishing = true) }

            val request = SpotRequest(
                trainModel = TrainModel(
                    model = state.value.rollingStockModel,
                    number = state.value.trainNumber,
                    carrierCode = state.value.selectedTrainSuggestion?.carrierCode ?: ""
                ),
                stationId = state.value.selectedStation?.id,
                trainRunId = null,
                description = state.value.description,
                lat = null,
                lon = null
            )

            spotRepository.updateSpot(id, request, state.value.selectedImageBytes)
                .onSuccess {
                    _events.send(AddSpotEvent.SpotPublished)
                }
                .onFailure { error ->
                    _events.send(AddSpotEvent.Error(error.toUiText()))
                }

            _state.update { it.copy(isPublishing = false) }
        }
    }
}
