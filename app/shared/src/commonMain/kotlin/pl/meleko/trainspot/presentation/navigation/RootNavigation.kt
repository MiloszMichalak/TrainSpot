package pl.meleko.trainspot.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val navigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Screen.Auth::class)
            subclass(Screen.Home::class)
        }
    }
}

@Composable
fun RootNavigation(modifier: Modifier) {
    val backStack = rememberNavBackStack(navigationConfig, Screen.Auth)

    Column(modifier) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            transitionSpec = NavTransitions.slideForward(),
            popTransitionSpec = NavTransitions.slideBackward(),
            predictivePopTransitionSpec = NavTransitions.slideBackwardPredictive(),
            entryProvider = entryProvider {
                entry<Screen.Auth>{
                    AuthNavigation(
                        onAuthSuccess = {
                            backStack.clear()
                            backStack.add(Screen.Home)
                        }
                    )
                }
                entry<Screen.Home>{

                }
            }
        )
    }
}
