package pl.meleko.trainspot.presentation.username

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pl.meleko.trainspot.presentation.components.GhostButton
import pl.meleko.trainspot.presentation.components.PrimaryButton
import pl.meleko.trainspot.presentation.components.RailFooter
import pl.meleko.trainspot.presentation.components.RailTextField
import pl.meleko.trainspot.presentation.util.ObserveAsEvents
import pl.meleko.trainspot.presentation.util.SnackbarController
import trainspot.app.shared.generated.resources.Res
import trainspot.app.shared.generated.resources.footer_text
import trainspot.app.shared.generated.resources.terms_of_service
import trainspot.app.shared.generated.resources.username_available
import trainspot.app.shared.generated.resources.username_label
import trainspot.app.shared.generated.resources.username_placeholder
import trainspot.app.shared.generated.resources.username_skip
import trainspot.app.shared.generated.resources.username_submit
import trainspot.app.shared.generated.resources.username_subtitle
import trainspot.app.shared.generated.resources.username_taken
import trainspot.app.shared.generated.resources.username_title_part1
import trainspot.app.shared.generated.resources.username_title_part2

@Composable
fun UsernameRoot(
    onSuccess: () -> Unit,
    viewModel: UsernameViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            UsernameEvent.Success -> onSuccess()
            is UsernameEvent.Error -> {
                SnackbarController.onEvent(event.message.asText())
            }
        }
    }

    UsernameScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun UsernameScreen(
    state: UsernameState,
    onAction: (UsernameAction) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            UsernameTitle()

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(Res.string.username_subtitle),
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 19.5.sp
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            RailTextField(
                label = stringResource(Res.string.username_label),
                value = state.username,
                onValueChange = { onAction(UsernameAction.OnUsernameChange(it)) },
                placeholder = stringResource(Res.string.username_placeholder),
                icon = Icons.Default.Person
            )

            if (state.isAvailable == true) {
                Text(
                    text = stringResource(Res.string.username_available),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colorScheme.primary
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else if (state.isAvailable == false) {
                Text(
                    text = stringResource(Res.string.username_taken),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colorScheme.error
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = stringResource(Res.string.username_submit),
                onClick = { onAction(UsernameAction.OnSubmit) },
                isLoading = state.isLoading,
                enabled = state.isAvailable == true && !state.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            GhostButton(
                text = stringResource(Res.string.username_skip),
                onClick = { onAction(UsernameAction.OnSkip) }
            )

            Spacer(modifier = Modifier.weight(1f))

            RailFooter(
                text = stringResource(Res.string.footer_text),
                actionText = stringResource(Res.string.terms_of_service),
                onActionClick = { /* Handle terms */ },
                version = "RAILSPOTTER v1.0"
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun UsernameTitle() {
    val colorScheme = MaterialTheme.colorScheme
    
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = colorScheme.onSurface)) {
            append(stringResource(Res.string.username_title_part1))
        }
        withStyle(style = SpanStyle(color = colorScheme.primary)) {
            append(stringResource(Res.string.username_title_part2))
        }
    }
    Text(
        text = annotatedString,
        style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 36.sp,
            letterSpacing = (-0.5).sp
        )
    )
}
