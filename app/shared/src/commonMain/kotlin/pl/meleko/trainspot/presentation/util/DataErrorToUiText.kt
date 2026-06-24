package pl.meleko.trainspot.presentation.util

import pl.meleko.trainspot.core.DataError
import trainspot.app.shared.generated.resources.Res
import trainspot.app.shared.generated.resources.error_client
import trainspot.app.shared.generated.resources.error_conflict
import trainspot.app.shared.generated.resources.error_disk_full
import trainspot.app.shared.generated.resources.error_invalid_data
import trainspot.app.shared.generated.resources.error_network
import trainspot.app.shared.generated.resources.error_not_found
import trainspot.app.shared.generated.resources.error_serialization
import trainspot.app.shared.generated.resources.error_server
import trainspot.app.shared.generated.resources.error_service_unavailable
import trainspot.app.shared.generated.resources.error_unauthorized
import trainspot.app.shared.generated.resources.error_unexpected
import trainspot.app.shared.generated.resources.error_unknown
import trainspot.app.shared.generated.resources.error_unknown_local

fun DataError.toUiText(): UiText {
    return when(this) {
        DataError.Network.SERVICE_UNAVAILABLE -> UiText.ResString(Res.string.error_service_unavailable)
        DataError.Network.CLIENT_ERROR -> UiText.ResString(Res.string.error_client)
        DataError.Network.SERVER_ERROR -> UiText.ResString(Res.string.error_server)
        DataError.Network.SERIALIZATION -> UiText.ResString(Res.string.error_serialization)
        DataError.Network.UNAUTHORIZED -> UiText.ResString(Res.string.error_unauthorized)
        DataError.Network.NOT_FOUND -> UiText.ResString(Res.string.error_not_found)
        DataError.Network.CONFLICT -> UiText.ResString(Res.string.error_conflict)
        DataError.Network.INVALID_DATA -> UiText.ResString(Res.string.error_invalid_data)
        DataError.Network.NETWORK -> UiText.ResString(Res.string.error_network)
        DataError.Network.UNKNOWN -> UiText.ResString(Res.string.error_unknown)
        DataError.Local.DISK_FULL -> UiText.ResString(Res.string.error_disk_full)
        DataError.Local.UNKNOWN -> UiText.ResString(Res.string.error_unknown_local)
        else -> UiText.ResString(Res.string.error_unexpected)
    }
}
