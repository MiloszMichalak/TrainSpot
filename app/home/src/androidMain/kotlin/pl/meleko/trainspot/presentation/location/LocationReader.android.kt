package pl.meleko.trainspot.presentation.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.maplibre.compose.location.createDefaultLocationProvider

@Composable
internal actual fun rememberLocationReader(): LocationReader {
    val context = LocalContext.current.applicationContext
    return remember(context) { MapLibreLocationReader { createDefaultLocationProvider(context) } }
}
