package pl.meleko.trainspot.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.meleko.trainspot.presentation.MainViewModel
import pl.meleko.trainspot.presentation.login.LoginViewModel
import pl.meleko.trainspot.presentation.register.RegisterViewModel

val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
}
