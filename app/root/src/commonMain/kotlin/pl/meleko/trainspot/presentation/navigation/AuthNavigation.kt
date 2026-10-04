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
import pl.meleko.trainspot.presentation.login.LoginRoot
import pl.meleko.trainspot.presentation.register.RegisterRoot
import pl.meleko.trainspot.presentation.username.UsernameRoot

private val authNavigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Screen.Auth.Login::class)
            subclass(Screen.Auth.Register::class)
            subclass(Screen.Auth.EnterUsername::class)
        }
    }
}

@Composable
fun AuthNavigation(
    onAuthenticated: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val backStack = rememberNavBackStack(authNavigationConfig, Screen.Auth.Login)

    NavDisplay(
        backStack = backStack,
        onBack = { 
            backStack.removeLastOrNull()
        },
        transitionSpec = NavTransitions.slideForward(),
        popTransitionSpec = NavTransitions.slideBackward(),
        predictivePopTransitionSpec = NavTransitions.slideBackwardPredictive(),
        entryProvider = entryProvider {
            entry<Screen.Auth.Login> {
                LoginRoot(
                    onLoginSuccess = {
                        onAuthenticated()
                        onAuthSuccess()
                    },
                    onNavigateToRegister = { backStack.add(Screen.Auth.Register) }
                )
            }
            entry<Screen.Auth.Register> {
                RegisterRoot(
                    onRegisterSuccess = {
                        onAuthenticated()
                        backStack.add(Screen.Auth.EnterUsername)
                    },
                    onNavigateToLogin = { backStack.add(Screen.Auth.Login) }
                )
            }
            entry<Screen.Auth.EnterUsername> {
                UsernameRoot(
                    onSuccess = { onAuthSuccess() }
                )
            }
        }
    )
}
