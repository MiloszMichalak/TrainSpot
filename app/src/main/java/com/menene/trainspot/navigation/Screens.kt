package com.menene.trainspot.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object RegisterScreen : NavKey

@Serializable
data object LoginScreen : NavKey

@Serializable
data object LandingScreen : NavKey

@Serializable
data object HomeScreen : NavKey

@Serializable
data object AddTrainScreen : NavKey
