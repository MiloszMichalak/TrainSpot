package com.menene.trainspot.auth.presentation.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.google.firebase.auth.FirebaseAuth
import com.menene.trainspot.home.HomeScreen
import com.menene.trainspot.auth.presentation.LandingScreen
import com.menene.trainspot.auth.presentation.LoginScreen
import com.menene.trainspot.auth.presentation.RegisterScreen
import org.koin.compose.koinInject

@Composable
fun NavigationRoot(
    snackbarHostState: SnackbarHostState,
    auth: FirebaseAuth = koinInject()
) {
    val backStack = rememberNavBackStack(
        if (auth.currentUser == null) LandingScreen else HomeScreen
    )

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