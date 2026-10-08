package pl.meleko.trainspot.presentation.addspot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.location.COARSE_LOCATION
import dev.icerock.moko.permissions.location.LOCATION
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import io.github.vinceglb.filekit.coil.securelyAccessFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.maplibre.compose.location.LocationUnavailableReason
import pl.meleko.trainspot.presentation.components.RailTextField
import pl.meleko.trainspot.presentation.location.LocationReadException
import pl.meleko.trainspot.presentation.location.rememberLocationReader
import pl.meleko.trainspot.presentation.util.ObserveAsEvents
import pl.meleko.trainspot.presentation.util.SnackbarController
import pl.meleko.trainspot.presentation.util.UiText
import trainspot.app.shared.generated.resources.Res
import trainspot.app.shared.generated.resources.add_photo_label
import trainspot.app.shared.generated.resources.add_photo_subtitle
import trainspot.app.shared.generated.resources.add_spot_title
import trainspot.app.shared.generated.resources.description_label
import trainspot.app.shared.generated.resources.description_placeholder
import trainspot.app.shared.generated.resources.destination_station_label
import trainspot.app.shared.generated.resources.error_location_permission_denied
import trainspot.app.shared.generated.resources.error_location_permission_settings
import trainspot.app.shared.generated.resources.error_location_services_disabled
import trainspot.app.shared.generated.resources.error_location_unavailable
import trainspot.app.shared.generated.resources.locate_station
import trainspot.app.shared.generated.resources.manual_train_error
import trainspot.app.shared.generated.resources.origin_station_label
import trainspot.app.shared.generated.resources.publish_button
import trainspot.app.shared.generated.resources.rolling_stock_model_label
import trainspot.app.shared.generated.resources.rolling_stock_model_placeholder
import trainspot.app.shared.generated.resources.rolling_stock_required_hint
import trainspot.app.shared.generated.resources.station_label
import trainspot.app.shared.generated.resources.station_placeholder
import trainspot.app.shared.generated.resources.train_number_label
import trainspot.app.shared.generated.resources.train_number_placeholder
import trainspot.app.shared.generated.resources.train_suggestions_header

@Composable
fun AddSpotRoot(
    spotId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddSpotViewModel = koinViewModel(parameters = { parametersOf(spotId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissionsFactory = rememberPermissionsControllerFactory()
    val permissions = remember(permissionsFactory) { permissionsFactory.createPermissionsController() }
    BindEffect(permissions)

    val locationReader = rememberLocationReader()

    LaunchedEffect(state.locationRequestId) {
        val requestId = state.locationRequestId ?: return@LaunchedEffect
        try {
            provideLocationPermission(permissions)
            val coordinates = locationReader.currentCoordinates()
            viewModel.onAction(AddSpotAction.OnLocationResolved(requestId, coordinates.latitude, coordinates.longitude))
        } catch (_: DeniedAlwaysException) {
            viewModel.onAction(AddSpotAction.OnLocationFailed(requestId, UiText.ResString(Res.string.error_location_permission_settings)))
        } catch (_: DeniedException) {
            viewModel.onAction(AddSpotAction.OnLocationFailed(requestId, UiText.ResString(Res.string.error_location_permission_denied)))
        } catch (_: TimeoutCancellationException) {
            viewModel.onAction(AddSpotAction.OnLocationFailed(requestId, UiText.ResString(Res.string.error_location_unavailable)))
        } catch (cancellation: CancellationException) {
            viewModel.onAction(AddSpotAction.OnLocationCancelled(requestId))
            throw cancellation
        } catch (failure: LocationReadException) {
            val message = when (failure.reason) {
                LocationUnavailableReason.ServicesDisabled -> Res.string.error_location_services_disabled
                LocationUnavailableReason.PermissionDenied -> Res.string.error_location_permission_denied
                else -> Res.string.error_location_unavailable
            }
            viewModel.onAction(AddSpotAction.OnLocationFailed(requestId, UiText.ResString(message)))
        } catch (_: Exception) {
            viewModel.onAction(AddSpotAction.OnLocationFailed(requestId, UiText.ResString(Res.string.error_location_unavailable)))
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            AddSpotEvent.NavigateBack -> onNavigateBack()
            AddSpotEvent.SpotPublished -> onNavigateBack()
            is AddSpotEvent.Error -> {
                SnackbarController.onEvent(event.message.asText())
            }
        }
    }

    AddSpotScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

private suspend fun provideLocationPermission(controller: PermissionsController) {
    try {
        controller.providePermission(Permission.LOCATION)
    } catch (failure: DeniedAlwaysException) {
        if (!controller.isPermissionGranted(Permission.COARSE_LOCATION)) throw failure
    } catch (failure: DeniedException) {
        if (!controller.isPermissionGranted(Permission.COARSE_LOCATION)) throw failure
    }
}

@Composable
fun AddSpotScreen(
    state: AddSpotState,
    onAction: (AddSpotAction) -> Unit
) {
    val pickerScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AddSpotHeader(
                onBackClick = { onAction(AddSpotAction.OnBackClick) },
                onPublishClick = { onAction(AddSpotAction.OnPublishClick) },
                isPublishing = state.isPublishing,
                canPublish = state.canPublish
            )
        },
        containerColor = Color(0xFF0D0E0F)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            PhotoZone(
                selectedMedia = state.selectedMedia,
                initialImageUrl = state.initialImageUrl,
                onClick = {
                    pickerScope.launch {
                        try {
                            val file = FileKit.openFilePicker(type = FileKitType.ImageAndVideo)
                                ?: return@launch
                            val mimeType = file.mimeType()
                            onAction(
                                AddSpotAction.OnMediaPicked(
                                    SelectedSpotMedia(
                                        file = file,
                                        fileName = file.name,
                                        contentType = mimeType?.toString()
                                    )
                                )
                            )
                        } catch (cancellation: CancellationException) {
                            throw cancellation
                        } catch (_: Throwable) {
                            onAction(AddSpotAction.OnMediaPickFailed)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RailTextField(
                label = stringResource(Res.string.station_label),
                value = state.stationSearchQuery,
                onValueChange = { onAction(AddSpotAction.OnStationSearchQueryChanged(it)) },
                placeholder = stringResource(Res.string.station_placeholder),
                icon = Icons.Default.LocationOn,
                keyboardType = KeyboardType.Text,
                trailingContent = {
                    IconButton(
                        onClick = { onAction(AddSpotAction.OnLocateStationClick) },
                        enabled = !state.isLocatingStation && !state.isLoading && !state.isPublishing
                    ) {
                        if (state.isLocatingStation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = stringResource(Res.string.locate_station),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )

            if (state.stationSuggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2124)),
                    border = BorderStroke(0.5.dp, Color(0xFFE8C547))
                ) {
                    Column {
                        state.stationSuggestions.forEach { station ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAction(AddSpotAction.OnStationSelected(station)) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = station.name,
                                    style = TextStyle(
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFF0EDE8)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (state.trainSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                TrainSuggestionsSection(
                    suggestions = state.trainSuggestions,
                    selectedSuggestion = state.selectedTrainSuggestion,
                    onSuggestionClick = { onAction(AddSpotAction.OnTrainSelected(it)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            RailTextField(
                label = stringResource(Res.string.train_number_label),
                value = state.trainNumber,
                onValueChange = { onAction(AddSpotAction.OnTrainNumberChanged(it)) },
                placeholder = stringResource(Res.string.train_number_placeholder),
                icon = Icons.Default.Close,
                keyboardType = KeyboardType.Text,
                trailingContent = {
                    IconButton(
                        onClick = { onAction(AddSpotAction.OnManualTrainClick) },
                        enabled = !state.isPublishing && !state.isLoading
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                }
            )
            if (state.showManualTrainRoute) {
                Spacer(Modifier.height(12.dp))
                RouteStationField(
                    label = stringResource(Res.string.origin_station_label),
                    query = state.originStationQuery,
                    suggestions = state.originStationSuggestions,
                    onQueryChange = { onAction(AddSpotAction.OnRouteStationQueryChanged(it, true)) },
                    onSelected = { onAction(AddSpotAction.OnRouteStationSelected(it, true)) }
                )
                Spacer(Modifier.height(12.dp))
                RouteStationField(
                    label = stringResource(Res.string.destination_station_label),
                    query = state.destinationStationQuery,
                    suggestions = state.destinationStationSuggestions,
                    onQueryChange = { onAction(AddSpotAction.OnRouteStationQueryChanged(it, false)) },
                    onSelected = { onAction(AddSpotAction.OnRouteStationSelected(it, false)) }
                )
            }
            if (!state.isTrainInputValid) {
                Text(
                    text = stringResource(Res.string.manual_train_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            RailTextField(
                label = stringResource(Res.string.rolling_stock_model_label),
                value = state.rollingStockModel,
                onValueChange = { onAction(AddSpotAction.OnRollingStockModelChanged(it)) },
                placeholder = stringResource(Res.string.rolling_stock_model_placeholder),
                icon = Icons.Default.Close,
                keyboardType = KeyboardType.Text
            )
            
            if (state.rollingStockModel.isBlank()) {
                Text(
                    text = stringResource(Res.string.rolling_stock_required_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            RailTextField(
                label = stringResource(Res.string.description_label),
                value = state.description,
                onValueChange = { onAction(AddSpotAction.OnDescriptionChanged(it)) },
                placeholder = stringResource(Res.string.description_placeholder),
                icon = Icons.Default.Close
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RouteStationField(
    label: String,
    query: String,
    suggestions: List<pl.meleko.trainspot.model.Station>,
    onQueryChange: (String) -> Unit,
    onSelected: (pl.meleko.trainspot.model.Station) -> Unit
) {
    Column {
        RailTextField(
            label = label,
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(Res.string.station_placeholder),
            icon = Icons.Default.LocationOn,
            keyboardType = KeyboardType.Text
        )
        suggestions.forEach { station ->
            TextButton(onClick = { onSelected(station) }, modifier = Modifier.fillMaxWidth()) {
                Text(station.name)
            }
        }
    }
}

@Composable
fun AddSpotHeader(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit,
    isPublishing: Boolean,
    canPublish: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0E0F))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFF1F2124))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = Color(0xFF9A9690)
            )
        }

        Text(
            text = stringResource(Res.string.add_spot_title),
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF0EDE8)
            )
        )

        Button(
            onClick = onPublishClick,
            modifier = Modifier.height(36.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE8C547),
                contentColor = Color(0xFF0D0E0F)
            ),
            enabled = canPublish,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        ) {
            if (isPublishing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color(0xFF0D0E0F),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(Res.string.publish_button),
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun PhotoZone(
    selectedMedia: SelectedSpotMedia?,
    initialImageUrl: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1A1C1F))
            .clickable { onClick() }
            .border(
                width = 1.5.dp,
                color = if (selectedMedia != null || initialImageUrl != null) Color(0xFFE8C547) else Color(0xFF3A3E45),
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selectedMedia != null) {
            val platformContext = LocalPlatformContext.current
            val imageLoader = remember(platformContext) {
                ImageLoader.Builder(platformContext)
                    .components { addPlatformFileSupport() }
                    .build()
            }
            AsyncImage(
                model = selectedMedia.file,
                imageLoader = imageLoader,
                contentDescription = selectedMedia.fileName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onState = { it.securelyAccessFile(selectedMedia.file) }
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFFE8C547).copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.add_photo_label),
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9A9690),
                        letterSpacing = 0.8.sp
                    )
                )
                Text(
                    text = stringResource(Res.string.add_photo_subtitle),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF5C5A56)
                    )
                )
            }
        }
    }
}

@Composable
private fun TrainSuggestionsSection(
    suggestions: List<TrainSuggestion>,
    selectedSuggestion: TrainSuggestion?,
    onSuggestionClick: (TrainSuggestion) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF7D))
            )
            Text(
                text = stringResource(Res.string.train_suggestions_header),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF5C5A56),
                    letterSpacing = 1.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestions, key = { it.trainOrderId }) { suggestion ->
                TrainChip(
                    suggestion = suggestion,
                    isSelected = suggestion.trainOrderId == selectedSuggestion?.trainOrderId,
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }
    }
}

@Composable
fun TrainChip(
    suggestion: TrainSuggestion,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFFE8C547).copy(alpha = 0.07f)
                else Color(0xFF1F2124)
            )
            .border(
                width = 0.5.dp,
                color = if (isSelected) Color(0xFFE8C547) else Color(0xFF3A3E45),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Text(
            text = suggestion.number,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE8C547)
            )
        )
        Text(
            text = "${suggestion.carrierCode} ${suggestion.trainName}",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF9A9690),
                letterSpacing = 0.4.sp
            )
        )
        Text(
            text = "${suggestion.time} → ${suggestion.stationName}",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color(0xFF5C5A56)
            ),
            maxLines = 2
        )
    }
}
