package com.menene.trainspot.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.menene.trainspot.data.AuthRepositoryImpl
import com.menene.trainspot.domain.repository.AuthRepository
import com.menene.trainspot.domain.use_case.ValidateEmail
import com.menene.trainspot.domain.use_case.ValidatePassword
import com.menene.trainspot.domain.use_case.ValidateRepeatedPassword
import com.menene.trainspot.presentation.AuthViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val AuthModule = module {
    single<FirebaseAuth> {
        Firebase.auth
    }

    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }

    singleOf(::ValidateEmail)
    singleOf(::ValidatePassword)
    singleOf(::ValidateRepeatedPassword)

    viewModelOf(::AuthViewModel)
}