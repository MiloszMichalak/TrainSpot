package pl.meleko.trainspot.di

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pl.meleko.trainspot.data.local.AppDatabase
import pl.meleko.trainspot.data.local.AuthDataStore
import pl.meleko.trainspot.data.local.getRoomDatabase
import pl.meleko.trainspot.data.remote.AuthService
import pl.meleko.trainspot.data.remote.DictionaryService
import pl.meleko.trainspot.data.remote.LikeService
import pl.meleko.trainspot.data.remote.SpotService
import pl.meleko.trainspot.data.remote.UserService
import pl.meleko.trainspot.data.repository.AuthRepositoryImpl
import pl.meleko.trainspot.data.repository.DictionaryRepositoryImpl
import pl.meleko.trainspot.data.repository.LikeRepositoryImpl
import pl.meleko.trainspot.data.repository.SpotRepositoryImpl
import pl.meleko.trainspot.data.repository.UserRepositoryImpl
import pl.meleko.trainspot.domain.repository.AuthRepository
import pl.meleko.trainspot.domain.repository.DictionaryRepository
import pl.meleko.trainspot.domain.repository.LikeRepository
import pl.meleko.trainspot.domain.repository.SpotRepository
import pl.meleko.trainspot.domain.repository.UserRepository

val dataModule = module {
    single { getRoomDatabase(get()) }
    single { get<AppDatabase>().stationDao() }
    single { get<AppDatabase>().carrierDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().spotDao() }

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
            install(Logging){
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.e(message)
                    }
                }
                level = LogLevel.ALL
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

    singleOf(::DictionaryService)
    singleOf(::DictionaryRepositoryImpl).bind(DictionaryRepository::class)

    singleOf(::SpotService)
    singleOf(::SpotRepositoryImpl).bind(SpotRepository::class)

    singleOf(::LikeService)
    singleOf(::LikeRepositoryImpl).bind(LikeRepository::class)

    singleOf(::UserService)
    singleOf(::UserRepositoryImpl).bind(UserRepository::class)
}
