package pl.meleko.trainspot.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.viewmodel.koinViewModel
import pl.meleko.trainspot.presentation.AuthState
import pl.meleko.trainspot.presentation.MainViewModel

private val navigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Screen.Auth::class)
            subclass(Screen.Home::class)
        }
    }
}

@Composable
fun RootNavigation(
    modifier: Modifier
) {
    val mainViewModel = koinViewModel<MainViewModel>()
    val authState by mainViewModel.authState.collectAsStateWithLifecycle()

    if (authState == AuthState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (authState == AuthState.Authenticated) Screen.Home else Screen.Auth
    val backStack = rememberNavBackStack(navigationConfig, startDestination)

    Column(modifier) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            transitionSpec = NavTransitions.slideForward(),
            popTransitionSpec = NavTransitions.slideBackward(),
            predictivePopTransitionSpec = NavTransitions.slideBackwardPredictive(),
            entryProvider = entryProvider {
                entry<Screen.Auth> {
                    AuthNavigation(
                        onAuthSuccess = {
                            backStack.removeLastOrNull()
                            backStack.add(Screen.Home)
                        }
                    )
                }
                entry<Screen.Home>{
                    HomeNavigation()
                }
            }
        )
    }
}
