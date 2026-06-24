package pl.meleko.trainspot.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pl.meleko.trainspot.data.local.AuthDataStore
import pl.meleko.trainspot.data.remote.AuthService
import pl.meleko.trainspot.data.repository.AuthRepositoryImpl
import pl.meleko.trainspot.domain.repository.AuthRepository

val dataModule = module {
    singleOf(::AuthDataStore)
    
    single {
        val authDataStore = get<AuthDataStore>()
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        authDataStore.getToken()?.let {
                            BearerTokens(it,null)
                        }
                    }
                }
            }
            defaultRequest {
                url("http://10.0.2.2:8080")
            }
        }
    }

    singleOf(::AuthService)
    singleOf(::AuthRepositoryImpl).bind(AuthRepository::class)
}
