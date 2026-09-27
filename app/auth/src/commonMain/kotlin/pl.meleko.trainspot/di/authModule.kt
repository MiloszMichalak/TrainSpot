package pl.meleko.trainspot.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pl.meleko.trainspot.data.remote.AuthService
import pl.meleko.trainspot.data.repository.AuthRepositoryImpl
import pl.meleko.trainspot.domain.AuthRepository
import pl.meleko.trainspot.presentation.login.LoginViewModel
import pl.meleko.trainspot.presentation.register.RegisterViewModel
import pl.meleko.trainspot.presentation.username.UsernameViewModel

val authModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::UsernameViewModel)

    singleOf(::AuthService)
    singleOf(::AuthRepositoryImpl).bind(AuthRepository::class)
}