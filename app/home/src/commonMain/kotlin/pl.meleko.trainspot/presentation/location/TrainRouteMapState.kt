package pl.meleko.trainspot.presentation.location

import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.model.StationStop
import pl.meleko.trainspot.presentation.util.UiText

data class TrainRouteMapState(
    val spot: Spot? = null,
    val stops: List<StationStop> = emptyList(),
    val isLoading: Boolean = true,
    val error: UiText? = null
)

sealed interface TrainRouteMapAction {
    data class Open(val spotId: String) : TrainRouteMapAction
    data object Retry : TrainRouteMapAction
}
