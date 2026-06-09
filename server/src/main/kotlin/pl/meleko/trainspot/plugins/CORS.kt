package pl.meleko.trainspot.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCORS() {
    val allowHosts = environment.config.propertyOrNull("cors.allowHost")
        ?.getString()
        ?.split(",")
        ?.map { it.trim() }?.distinct()
        ?: listOf("localhost:3000", "localhost:8080")

    install(CORS) {
        allowHosts.forEach { host ->
            allowHost(host, schemes = listOf("http", "https"))
        }

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Options)
        allowCredentials = true
        maxAgeInSeconds = 3600
    }
}

