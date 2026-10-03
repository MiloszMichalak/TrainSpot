package pl.meleko.trainspot.presentation.location

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.layers.RasterLayer
import org.maplibre.compose.overlay.DisappearingCompassButton
import org.maplibre.compose.overlay.DisappearingScaleBar
import org.maplibre.compose.sources.rememberRasterTileSource
import org.maplibre.spatialk.geojson.Point
import trainspot.app.shared.generated.resources.Res
import trainspot.app.shared.generated.resources.navigate_back
import trainspot.app.shared.generated.resources.spot_location_title

@Composable
fun SpotLocationMapScreen(
    placeName: String,
    latitude: Double,
    longitude: Double,
    onNavigateBack: () -> Unit
) {
    val mapState = rememberMapState(
        baseStyle = if (isSystemInDarkTheme()) {
            BaseStyle.Uri("https://tiles.openfreemap.org/styles/dark")
        } else {
            BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty")
        },
        initialCameraPosition = CameraPosition(
            target = Position(latitude = latitude, longitude = longitude),
            zoom = 14.0
        )
    ) {
        val railwaySource = rememberRasterTileSource(
            tiles = listOf(
                "https://tiles.openrailwaymap.org/standard/{z}/{x}/{y}.png"
            ),
            tileSize = 256
        )

        RasterLayer(
            id = "openrailwaymap",
            source = railwaySource,
            opacity = const(1f)
        )

        val spotLocation = rememberGeoJsonSource(
            GeoJsonData.Features(
                Point(
                    longitude = longitude,
                    latitude = latitude,
                )
            )
        )

        CircleLayer(
            id = "spot-location-marker",
            source = spotLocation,
            radius = const(10.dp),
            color = const(Color(0xFFE8C547)),
            strokeColor = const(Color.White),
            strokeWidth = const(3.dp)
        )
    }

    Box(Modifier.fillMaxSize()) {
        MaplibreMap(
            state = mapState,
            modifier = Modifier.fillMaxSize()
        ) {
            DisappearingScaleBar(
                metersPerDp = { mapState.viewport?.metersPerDpAtTarget ?: 1.0 },
                zoom = { mapState.cameraPosition.zoom},
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            )

            DisappearingCompassButton(
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
        TopAppBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp)),
            windowInsets = WindowInsets(0, 0, 0, 0),
            title = {
                Text(
                    text = placeName.ifBlank { stringResource(Res.string.spot_location_title) },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.navigate_back)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF141618).copy(alpha = 0.92f),
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
