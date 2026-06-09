package pl.meleko.trainspot.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun String.toInstant(): Instant {
    return LocalDateTime.parse(this)
        .toInstant(TimeZone.of("Europe/Warsaw"))
}

fun Instant?.toDateString(): String {
    return this?.toLocalDateTime(TimeZone.of("Europe/Warsaw")).toString()
}