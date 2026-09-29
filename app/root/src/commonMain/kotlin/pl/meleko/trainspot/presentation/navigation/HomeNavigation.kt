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
import pl.meleko.trainspot.presentation.addspot.AddSpotRoot
import pl.meleko.trainspot.presentation.feed.FeedRoot
import pl.meleko.trainspot.presentation.profile.ProfileRoot

private val homeNavigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Screen.Home.Feed::class)
            subclass(Screen.Home.Details::class)
            subclass(Screen.Home.Create::class)
            subclass(Screen.Home.Profile::class)
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
                    onNavigateToDetails = { /* backStack.add(Screen.Home.Details) */ },
                    onNavigateToProfile = { backStack.add(Screen.Home.Profile) }
                )
            }
            entry<Screen.Home.Create> {
                AddSpotRoot(
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
            entry<Screen.Home.Details> {
                // TODO: Implement SpotDetailsRoot
            }
            entry<Screen.Home.Profile> {
                ProfileRoot(onNavigateBack = { backStack.removeLastOrNull() })
            }
        }
    )
}
