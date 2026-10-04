package pl.meleko.trainspot.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import org.koin.compose.viewmodel.koinViewModel
import pl.meleko.trainspot.presentation.AuthState
import pl.meleko.trainspot.presentation.MainViewModel

private val navigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Screen.Auth::class, Screen.Auth.serializer())
            subclass(Screen.Home::class, Screen.Home.serializer())
        }
    }
}

@Composable
fun RootNavigation(
    modifier: Modifier,
    contentPadding: PaddingValues
) {
    val mainViewModel = koinViewModel<MainViewModel>()
    val authState by mainViewModel.authState.collectAsStateWithLifecycle()

    if (authState == AuthState.Loading) {
        Box(
            modifier = modifier.fillMaxSize().padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (authState == AuthState.Authenticated) Screen.Home else Screen.Auth
    val backStack = rememberNavBackStack(navigationConfig, startDestination)

    Column(modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            transitionSpec = NavTransitions.slideForward(),
            popTransitionSpec = NavTransitions.slideBackward(),
            predictivePopTransitionSpec = NavTransitions.slideBackwardPredictive(),
            entryProvider = entryProvider {
                entry<Screen.Auth> {
                    Box(Modifier.fillMaxSize().padding(contentPadding)) {
                        AuthNavigation(
                            onAuthenticated = mainViewModel::onAuthenticated,
                            onAuthSuccess = {
                                backStack.removeLastOrNull()
                                backStack.add(Screen.Home)
                            }
                        )
                    }
                }
                entry<Screen.Home>{
                    HomeNavigation(contentPadding = contentPadding)
                }
            }
        )
    }
}
