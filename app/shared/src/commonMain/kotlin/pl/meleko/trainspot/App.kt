package pl.meleko.trainspot

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pl.meleko.trainspot.presentation.navigation.RootNavigation
import pl.meleko.trainspot.presentation.theme.TrainSpotTheme

@Composable
fun App() {
    TrainSpotTheme {
        Scaffold { innerPadding ->
            RootNavigation(Modifier.padding(innerPadding))
        }
    }
}
