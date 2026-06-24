package pl.meleko.trainspot.di

import android.content.Context
import org.koin.dsl.module
import pl.meleko.trainspot.data.local.createDataStore
import pl.meleko.trainspot.data.local.getDatabaseBuilder

actual val platformModule = module {
    single { createDataStore(get<Context>()) }

    single { getDatabaseBuilder(get<Context>()) }
}