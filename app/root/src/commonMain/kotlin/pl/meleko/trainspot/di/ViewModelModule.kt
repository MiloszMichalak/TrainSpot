package pl.meleko.trainspot.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.meleko.trainspot.presentation.MainViewModel

val viewModelModule = module {
    viewModelOf(::MainViewModel)
}
