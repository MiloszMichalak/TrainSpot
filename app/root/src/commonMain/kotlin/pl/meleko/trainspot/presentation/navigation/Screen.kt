package pl.meleko.trainspot.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen: NavKey {
    @Serializable
    data object Auth: Screen {
        @Serializable
        data object Login: Screen
        @Serializable
        data object Register: Screen
        @Serializable
        data object EnterUsername: Screen
    }

    @Serializable
    data object Home: Screen {
        @Serializable
        data object Feed: Screen
        @Serializable
        data object Details: Screen
        @Serializable
        data object Create: Screen
    }
}