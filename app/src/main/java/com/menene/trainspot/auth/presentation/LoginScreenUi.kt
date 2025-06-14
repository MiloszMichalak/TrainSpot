package com.menene.trainspot.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.menene.trainspot.R
import com.menene.trainspot.auth.presentation.components.EmailTextField
import com.menene.trainspot.auth.presentation.components.PasswordTextField
import com.menene.trainspot.auth.presentation.components.WideButton
import com.menene.trainspot.auth.presentation.model.AuthFormEvent
import com.menene.trainspot.auth.presentation.model.AuthType
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    snackbarHostState: SnackbarHostState,
    onSuccessfulLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AuthViewModel = koinViewModel()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val event = viewModel.validationEvent

    LaunchedEffect(event) {
        event.collect { authEvent ->
            when (authEvent) {
                is AuthViewModel.AuthEvent.Success -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.logged_succes))
                    }
                    onSuccessfulLogin()
                }
                is AuthViewModel.AuthEvent.Failure -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(context.getString(authEvent.errorId))
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(R.string.login),
            style = MaterialTheme.typography.headlineLarge
        )

        EmailTextField(
            value = viewModel.state.email,
            onValueChange = {
                viewModel.onEvent(AuthFormEvent.EmailChanged(it))
            },
            error = viewModel.state.emailError
        )

        PasswordTextField(
            value = viewModel.state.password,
            placeholderId = R.string.password,
            onValueChange = {
                viewModel.onEvent(AuthFormEvent.PasswordChanged(it))
            },
            error = viewModel.state.passwordError
        )

        WideButton(
            stringId = R.string.login,
            onClick = {
                viewModel.onEvent(AuthFormEvent.Submit(AuthType.Login))
            }
        )
    }
}