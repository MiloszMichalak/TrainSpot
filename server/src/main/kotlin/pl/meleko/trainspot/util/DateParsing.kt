package pl.meleko.trainspot.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

fun String?.toInstant(): Instant {
    if (this == null) return Clock.System.now()

    return LocalDateTime.parse(this)
        .toInstant(TimeZone.of("Europe/Warsaw"))
}

fun Instant?.toDateString(): String {
    return this?.toLocalDateTime(TimeZone.of("Europe/Warsaw")).toString()
}

fun String?.toLocalTime(): LocalTime? {
    if (this.isNullOrBlank()) return null

    return LocalTime.parse(this)
}

fun String.toLocalDate(): LocalDate {
    return LocalDate.parse(this)
}