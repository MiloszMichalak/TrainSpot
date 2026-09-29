package pl.meleko.trainspot.presentation.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import trainspot.app.shared.generated.resources.Res
import trainspot.app.shared.generated.resources.time_date_format
import trainspot.app.shared.generated.resources.time_days_ago
import trainspot.app.shared.generated.resources.time_hours_ago
import trainspot.app.shared.generated.resources.time_just_now
import trainspot.app.shared.generated.resources.time_minutes_ago
import androidx.compose.runtime.Composable
import kotlinx.datetime.number
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun Instant.toRelativeTimeString(now: Instant = Clock.System.now()): String {
    val elapsedMinutes = (now - this).inWholeMinutes.coerceAtLeast(0)
    return when {
        elapsedMinutes < 1 -> stringResource(Res.string.time_just_now)
        elapsedMinutes < 60 -> pluralStringResource(Res.plurals.time_minutes_ago, elapsedMinutes.toInt(), elapsedMinutes.toInt())
        elapsedMinutes < 24 * 60 -> (elapsedMinutes / 60).toInt().let { hours ->
            pluralStringResource(Res.plurals.time_hours_ago, hours, hours)
        }
        elapsedMinutes < 7 * 24 * 60 -> (elapsedMinutes / (24 * 60)).toInt().let { days ->
            pluralStringResource(Res.plurals.time_days_ago, days, days)
        }
        else -> toLocalDateTime(TimeZone.currentSystemDefault()).date.let { date ->
            stringResource(
                Res.string.time_date_format,
                date.day.toString().padStart(2, '0'),
                date.month.number.toString().padStart(2, '0'),
                date.year.toString()
            )
        }
    }
}
