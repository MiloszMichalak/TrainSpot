package pl.meleko.trainspot.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.navigation3.scene.Scene

object NavTransitions {
    const val DEFAULT_DURATION = 350

    fun <T : Any> slideForward(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(DEFAULT_DURATION)
        ) togetherWith slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(DEFAULT_DURATION)
        )
    }

    fun <T : Any> slideBackward(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(DEFAULT_DURATION)
        ) togetherWith slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(DEFAULT_DURATION)
        )
    }

    fun <T : Any> slideBackwardPredictive(): AnimatedContentTransitionScope<Scene<T>>.(Int) -> ContentTransform = { _ ->
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(DEFAULT_DURATION)
        ) togetherWith slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(DEFAULT_DURATION)
        )
    }
}
