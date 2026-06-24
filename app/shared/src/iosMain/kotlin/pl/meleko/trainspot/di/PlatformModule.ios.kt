package pl.meleko.trainspot.di

import org.koin.dsl.module
import pl.meleko.trainspot.data.local.createDataStore
import pl.meleko.trainspot.data.local.getDatabaseBuilder

actual val platformModule = module {
    single {
        createDataStore()
    }

    single {
        getDatabaseBuilder()
    }
}