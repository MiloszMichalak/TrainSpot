package com.menene.trainspot.presentation.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.menene.trainspot.presentation.HomeScreen
import com.menene.trainspot.presentation.LandingScreen
import com.menene.trainspot.presentation.LoginScreen
import com.menene.trainspot.presentation.RegisterScreen

@Composable
fun NavigationRoot(
    snackbarHostState: SnackbarHostState
) {
    val backStack = rememberNavBackStack(LandingScreen)

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
            rememberSceneSetupNavEntryDecorator()
        ),
        entryProvider = { key ->
            when (key) {
                is LoginScreen -> {
                    NavEntry(key = key) {
                        LoginScreen(
                            snackbarHostState,
                            onSuccessfulLogin = {
                                backStack.clear()
                                backStack.add(HomeScreen)
                            }
                        )
                    }
                }
                is RegisterScreen -> {
                    NavEntry(key = key) {
                        RegisterScreen(
                            snackbarHostState,
                            onSuccesfulRegister = {
                                backStack.clear()
                                backStack.add(HomeScreen)
                            }
                        )
                    }
                }
                is LandingScreen -> {
                    NavEntry(key = key) {
                        LandingScreen(
                            onSignUpClicked = {
                                backStack.add(RegisterScreen)
                            },
                            onSingInClicked = {
                                backStack.add(LoginScreen)
                            }
                        )
                    }
                }
                is HomeScreen -> {
                    NavEntry(key = key) {
                        HomeScreen()
                    }
                }
                else -> throw IllegalArgumentException("Unknown screen: $key")
            }
        }
    )
}