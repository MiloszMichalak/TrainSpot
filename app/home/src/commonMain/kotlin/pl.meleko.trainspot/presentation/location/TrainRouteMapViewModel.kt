package pl.meleko.trainspot.presentation.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.domain.ScheduleRepository
import pl.meleko.trainspot.domain.SpotRepository
import pl.meleko.trainspot.presentation.util.toUiText

class TrainRouteMapViewModel(
    private val spotRepository: SpotRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TrainRouteMapState())
    val state = _state.asStateFlow()
    private var spotId: String? = null
    private var request: Job? = null

    fun onAction(action: TrainRouteMapAction) {
        when (action) {
            is TrainRouteMapAction.Open -> {
                if (spotId == action.spotId) return
                request?.cancel()
                spotId = action.spotId
                _state.value = TrainRouteMapState()
                load()
            }
            TrainRouteMapAction.Retry -> if (request?.isActive != true) load()
        }
    }

    private fun load() {
        val id = spotId ?: return
        request = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val spot = _state.value.spot ?: when (val result = spotRepository.getSpot(id)) {
                is Result.Success -> result.data.also { loaded -> _state.update { it.copy(spot = loaded) } }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.error.toUiText()) }
                    return@launch
                }
            }
            when (val result = scheduleRepository.getTrainRoute(spot.trainRun.trainOrderId)) {
                is Result.Success -> _state.update {
                    it.copy(stops = result.data.stations.sortedBy { stop -> stop.orderNumber }, isLoading = false)
                }
                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = result.error.toUiText())
                }
            }
        }
    }
}
