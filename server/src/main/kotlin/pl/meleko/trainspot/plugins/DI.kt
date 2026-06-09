package pl.meleko.trainspot.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.plugin.Koin
import pl.meleko.trainspot.di.appModule

fun Application.configureDI() {
    install(Koin) {
        modules(appModule)
    }
}



