package pl.meleko.trainspot.presentation.location

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import trainspot.app.shared.generated.resources.Res
import trainspot.app.shared.generated.resources.comments_retry
import trainspot.app.shared.generated.resources.train_route_empty
import trainspot.app.shared.generated.resources.train_route_title

@Composable
fun TrainRouteMapRoot(
    spotId: String,
    onNavigateBack: () -> Unit,
    viewModel: TrainRouteMapViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(spotId) { viewModel.onAction(TrainRouteMapAction.Open(spotId)) }

    TrainRouteMapScreen(
        state = state,
        onRetry = { viewModel.onAction(TrainRouteMapAction.Retry) },
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun TrainRouteMapScreen(
    state: TrainRouteMapState,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val spot = state.spot
    val isManualTrain = (spot?.trainRun?.trainOrderId ?: 0) < 0
    val routeName = listOfNotNull(spot?.trainRun?.originStation?.name, spot?.trainRun?.destStation?.name)
        .filter { it.isNotBlank() }.joinToString(" → ")

    key(spot?.id) {
        SpotLocationMapScreen(
            placeName = routeName.ifBlank { stringResource(Res.string.train_route_title) },
            latitude = spot?.lat ?: 52.0,
            longitude = spot?.lon ?: 19.0,
            onNavigateBack = onNavigateBack,
            bottomContent = { onFocusStation ->
                if (!isManualTrain) TrainRouteStops(state, onRetry, onFocusStation)
            }
        )
    }
}

@Composable
private fun TrainRouteStops(
    state: TrainRouteMapState,
    onRetry: () -> Unit,
    onFocusStation: (Double, Double) -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(
            modifier = Modifier.fillMaxWidth().windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
            ).padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.train_route_title),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))

                state.error != null -> Column(Modifier.padding(horizontal = 16.dp)) {
                    Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onRetry) { Text(stringResource(Res.string.comments_retry)) }
                }

                state.stops.isEmpty() -> Text(
                    text = stringResource(Res.string.train_route_empty),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                else -> {
                    val spotStationId = state.spot?.station?.id
                    val spotStationIndex = state.stops.indexOfFirst { it.stationId.id == spotStationId }
                    val listState = rememberLazyListState(
                        initialFirstVisibleItemIndex = spotStationIndex.coerceAtLeast(0)
                    )

                    LazyRow(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            state.stops,
                            key = { "${it.orderNumber}:${it.stationId.id}" }
                        ) { stop ->
                            val isSpotStation = stop.stationId.id == spotStationId
                            val station = stop.stationId

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (stop != state.stops.first()) Text("→", color = MaterialTheme.colorScheme.primary)

                                Surface(
                                    modifier = Modifier.clickable {
                                        onFocusStation(station.latitude, station.longitude)
                                    },
                                    shape = MaterialTheme.shapes.medium,
                                    color = if (isSpotStation) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = if (isSpotStation) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                ) {
                                    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                        Text(stop.stationId.name, style = MaterialTheme.typography.bodyMedium)

                                        val time = stop.departureTime ?: stop.arrivalTime

                                        if (!time.isNullOrBlank()) Text(
                                            time,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSpotStation) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
