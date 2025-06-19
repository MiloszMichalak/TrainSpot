package com.menene.trainspot.di

import com.menene.trainspot.home.data.FirebaseTrainRepositoryImpl
import com.menene.trainspot.home.data.TrainStorage
import com.menene.trainspot.home.domain.FirebaseTrainRepository
import com.menene.trainspot.home.presentation.AddTrainViewModel
import com.menene.trainspot.home.util.ImageCompressor
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val HomeModule = module {
    singleOf(::ImageCompressor)

    viewModelOf(::AddTrainViewModel)

    singleOf(::FirebaseTrainRepositoryImpl) { bind<FirebaseTrainRepository>() }

    single {
        TrainStorage()
    }
}