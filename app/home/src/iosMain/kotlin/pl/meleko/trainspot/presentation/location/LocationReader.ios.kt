package pl.meleko.trainspot.presentation.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.maplibre.compose.location.AppleLocationProvider

@Composable
internal actual fun rememberLocationReader(): LocationReader =
    remember { MapLibreLocationReader { AppleLocationProvider() } }
