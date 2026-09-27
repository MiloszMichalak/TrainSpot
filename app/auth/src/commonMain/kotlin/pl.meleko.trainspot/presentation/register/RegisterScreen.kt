package pl.meleko.trainspot.presentation.register

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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import pl.meleko.trainspot.presentation.components.DividerRow
import pl.meleko.trainspot.presentation.components.GhostButton
import pl.meleko.trainspot.presentation.components.PrimaryButton
import pl.meleko.trainspot.presentation.components.RailFooter
import pl.meleko.trainspot.presentation.components.RailTextField
import pl.meleko.trainspot.presentation.util.ObserveAsEvents
import trainspot.app.shared.generated.resources.Res
import trainspot.app.shared.generated.resources.email
import trainspot.app.shared.generated.resources.email_placeholder
import trainspot.app.shared.generated.resources.footer_text
import trainspot.app.shared.generated.resources.login_ghost_button
import trainspot.app.shared.generated.resources.password
import trainspot.app.shared.generated.resources.password_placeholder
import trainspot.app.shared.generated.resources.register_button
import trainspot.app.shared.generated.resources.register_subtitle
import trainspot.app.shared.generated.resources.register_title_part1
import trainspot.app.shared.generated.resources.register_title_part2
import trainspot.app.shared.generated.resources.repeat_password
import trainspot.app.shared.generated.resources.terms_of_service

@Composable
fun RegisterRoot(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            RegisterEvent.RegisterSuccess -> onRegisterSuccess()
            is RegisterEvent.Error -> { /* Show Snackbar or toast */ }
        }
    }

    RegisterScreen(
        state = state,
        onAction = { action ->
            when(action) {
                RegisterAction.OnLoginClick -> onNavigateToLogin()
                else -> viewModel.onAction(action)
            }
        }
    )
}

@Composable
fun RegisterScreen(
    state: RegisterState,
    onAction: (RegisterAction) -> Unit
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

            RegisterTitle()

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(Res.string.register_subtitle),
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 19.5.sp
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            RailTextField(
                label = stringResource(Res.string.email),
                value = state.email,
                onValueChange = { onAction(RegisterAction.OnEmailChange(it)) },
                placeholder = stringResource(Res.string.email_placeholder),
                icon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(12.dp))

            RailTextField(
                label = stringResource(Res.string.password),
                value = state.password,
                onValueChange = { onAction(RegisterAction.OnPasswordChange(it)) },
                placeholder = stringResource(Res.string.password_placeholder),
                icon = Icons.Default.Lock,
                isPassword = true,
                isPasswordVisible = state.isPasswordVisible,
                onToggleVisibility = { onAction(RegisterAction.OnTogglePasswordVisibility) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RailTextField(
                label = stringResource(Res.string.repeat_password),
                value = state.repeatPassword,
                onValueChange = { onAction(RegisterAction.OnRepeatPasswordChange(it)) },
                placeholder = stringResource(Res.string.password_placeholder),
                icon = Icons.Default.Lock,
                isPassword = true,
                isPasswordVisible = state.isPasswordVisible,
                onToggleVisibility = { onAction(RegisterAction.OnTogglePasswordVisibility) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = stringResource(Res.string.register_button),
                onClick = { onAction(RegisterAction.OnRegisterClick) },
                isLoading = state.isLoading
            )

            DividerRow()

            GhostButton(
                text = stringResource(Res.string.login_ghost_button),
                onClick = { onAction(RegisterAction.OnLoginClick) }
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
fun RegisterTitle() {
    val colorScheme = MaterialTheme.colorScheme
    
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = colorScheme.onSurface)) {
            append(stringResource(Res.string.register_title_part1))
        }
        withStyle(style = SpanStyle(color = colorScheme.primary)) {
            append(stringResource(Res.string.register_title_part2))
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
