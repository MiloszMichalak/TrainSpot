package pl.meleko.trainspot.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import pl.meleko.trainspot.remote.PkpApiClient
import pl.meleko.trainspot.remote.PkpDataSeeder

val appModule = module {
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
            install(DefaultRequest) {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "pdp-api.plk-sa.pl"
                }

                header("Content-Type", "application/json")
                header("X-Api-Key", System.getenv("PKP_API_KEY") ?: error("PKP_API_KEY not found"))
            }
        }
    }

    singleOf(::PkpApiClient)
    singleOf(::PkpDataSeeder)
}