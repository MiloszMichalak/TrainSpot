package pl.meleko.trainspot.remote

import io.ktor.server.application.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

fun Application.configureScheduler(){
    val pkpDataSeeder by inject<PkpDataSeeder>()

    launch(Dispatchers.IO){
        while (isActive) {
            val zoneId = ZoneId.of("Europe/Warsaw")
            val now = LocalDateTime.now(zoneId)

            var nextRun = now.withHour(2).withMinute(0).withSecond(0)

            if (now.isAfter(nextRun) || now.isEqual(nextRun)) {
                nextRun = nextRun.plusDays(1)
            }

            val delayMillis = Duration.between(now, nextRun).toMillis()
            delay(delayMillis.milliseconds)

            try {
                pkpDataSeeder.seedAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}