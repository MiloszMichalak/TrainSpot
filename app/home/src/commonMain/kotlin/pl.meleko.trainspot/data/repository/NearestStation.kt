package pl.meleko.trainspot.data.repository

import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.measurement.distance
import org.maplibre.spatialk.units.extensions.inMeters
import pl.meleko.trainspot.model.Station

internal fun nearestStation(
    stations: List<Station>,
    latitude: Double,
    longitude: Double,
    maxDistanceMeters: Double,
): Station? {
    if (
        !validCoordinates(latitude, longitude) ||
        !maxDistanceMeters.isFinite() ||
        maxDistanceMeters < 0.0
    ) {
        return null
    }

    val location = Position(
        longitude = longitude,
        latitude = latitude,
    )

    var nearest: Station? = null
    var nearestDistance = maxDistanceMeters

    for (station in stations) {
        if (
            validCoordinates(station.latitude, station.longitude) ||
            (station.latitude != 0.0 && station.longitude != 0.0)
        ) {
            val stationPosition = Position(
                longitude = station.longitude,
                latitude = station.latitude,
            )

            val distanceMeters = distance(
                location,
                stationPosition,
            ).inMeters

            val currentNearest = nearest

            if (
                distanceMeters < nearestDistance ||
                (
                        distanceMeters == nearestDistance &&
                                (currentNearest == null || station.id < currentNearest.id)
                        )
            ) {
                nearest = station
                nearestDistance = distanceMeters
            }
        }
    }

    return nearest
}

private fun validCoordinates(
    latitude: Double,
    longitude: Double,
): Boolean =
    latitude.isFinite() &&
            longitude.isFinite() &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0
