package pl.meleko.trainspot

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import pl.meleko.trainspot.presentation.navigation.RootNavigation
import pl.meleko.trainspot.presentation.theme.TrainSpotTheme
import pl.meleko.trainspot.presentation.util.ObserveAsEvents
import pl.meleko.trainspot.presentation.util.SnackbarController

@Composable
fun App() {
    TrainSpotTheme {
        val snackbarState = remember { SnackbarHostState() }

        ObserveAsEvents(SnackbarController.events){ event ->
            snackbarState.showSnackbar(event)
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarState) }
        ) { innerPadding ->
            RootNavigation(
                modifier = Modifier,
                contentPadding = innerPadding
            )
        }
    }
}
