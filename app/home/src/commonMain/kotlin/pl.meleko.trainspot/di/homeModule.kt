package pl.meleko.trainspot.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pl.meleko.trainspot.data.remote.DictionaryService
import pl.meleko.trainspot.data.remote.CommentService
import pl.meleko.trainspot.data.remote.LikeService
import pl.meleko.trainspot.data.remote.ScheduleService
import pl.meleko.trainspot.data.remote.SpotService
import pl.meleko.trainspot.data.repository.DictionaryRepositoryImpl
import pl.meleko.trainspot.data.repository.CommentRepositoryImpl
import pl.meleko.trainspot.data.repository.LikeRepositoryImpl
import pl.meleko.trainspot.data.repository.ScheduleRepositoryImpl
import pl.meleko.trainspot.data.repository.SpotRepositoryImpl
import pl.meleko.trainspot.domain.DictionaryRepository
import pl.meleko.trainspot.domain.CommentRepository
import pl.meleko.trainspot.domain.LikeRepository
import pl.meleko.trainspot.domain.ScheduleRepository
import pl.meleko.trainspot.domain.SpotRepository
import pl.meleko.trainspot.presentation.addspot.AddSpotViewModel
import pl.meleko.trainspot.presentation.feed.FeedViewModel
import pl.meleko.trainspot.presentation.comments.CommentsViewModel
import pl.meleko.trainspot.presentation.profile.ProfileViewModel

val homeModule = module {
    viewModelOf(::FeedViewModel)
    viewModelOf(::CommentsViewModel)
    viewModelOf(::ProfileViewModel)

    viewModel {
        AddSpotViewModel(null, get(), get(), get())
    }

    singleOf(::DictionaryService)
    singleOf(::DictionaryRepositoryImpl).bind(DictionaryRepository::class)

    singleOf(::SpotService)
    singleOf(::SpotRepositoryImpl).bind(SpotRepository::class)

    singleOf(::LikeService)
    singleOf(::LikeRepositoryImpl).bind(LikeRepository::class)

    singleOf(::CommentService)
    singleOf(::CommentRepositoryImpl).bind(CommentRepository::class)

    singleOf(::ScheduleService)
    singleOf(::ScheduleRepositoryImpl).bind(ScheduleRepository::class)

}
