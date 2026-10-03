package pl.meleko.trainspot.presentation.location

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.LocationRequest
import org.maplibre.compose.location.LocationUnavailableReason
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.seconds

internal interface LocationReader {
    suspend fun currentCoordinates(): Position
}

@Composable
internal expect fun rememberLocationReader(): LocationReader

internal class LocationReadException(val reason: LocationUnavailableReason) : Exception()

internal class MapLibreLocationReader(
    private val createProvider: () -> LocationProvider
) : LocationReader {
    override suspend fun currentCoordinates(): Position {
        val provider = createProvider()

        try {
            val update = withTimeout(20.seconds) {
                provider.updates(LocationRequest()).first { event ->
                    when (event) {
                        is LocationEvent.Update -> {
                            val position = event.measurement.position
                            event.measurementMark.elapsedNow() <= 30.seconds &&
                                position.latitude.isFinite() && position.longitude.isFinite() &&
                                position.latitude in -90.0..90.0 && position.longitude in -180.0..180.0
                        }
                        is LocationEvent.Unavailable -> {
                            if (event.reason != LocationUnavailableReason.TemporarilyUnavailable) {
                                throw LocationReadException(event.reason)
                            }
                            false
                        }
                    }
                } as LocationEvent.Update
            }

            return update.measurement.position
        } finally {
            provider.close()
        }
    }
}
