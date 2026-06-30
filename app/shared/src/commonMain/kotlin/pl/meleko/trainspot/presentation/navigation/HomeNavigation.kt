package pl.meleko.trainspot.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import pl.meleko.trainspot.presentation.feed.FeedRoot

private val homeNavigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Screen.Home.Feed::class)
            subclass(Screen.Home.Details::class)
            subclass(Screen.Home.Create::class)
        }
    }
}

@Composable
fun HomeNavigation() {
    val backStack = rememberNavBackStack(homeNavigationConfig, Screen.Home.Feed)

    NavDisplay(
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        transitionSpec = NavTransitions.slideForward(),
        popTransitionSpec = NavTransitions.slideBackward(),
        predictivePopTransitionSpec = NavTransitions.slideBackwardPredictive(),
        entryProvider = entryProvider {
            entry<Screen.Home.Feed> {
                FeedRoot(
                    onNavigateToCreate = { backStack.add(Screen.Home.Create) },
                    onNavigateToDetails = { /* backStack.add(Screen.Home.Details) */ }
                )
            }
            entry<Screen.Home.Create> {

            }
            entry<Screen.Home.Details> {
                // TODO: Implement SpotDetailsRoot
            }
        }
    )
}
