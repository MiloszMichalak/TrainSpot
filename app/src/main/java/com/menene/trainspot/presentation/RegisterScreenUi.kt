package com.menene.trainspot.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.menene.trainspot.R
import com.menene.trainspot.presentation.components.EmailTextField
import com.menene.trainspot.presentation.components.PasswordTextField
import com.menene.trainspot.presentation.components.WideButton
import com.menene.trainspot.presentation.model.AuthFormEvent
import com.menene.trainspot.presentation.model.AuthType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: AuthViewModel = koinViewModel()

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(R.string.register),
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
            onValueChange = {
                viewModel.onEvent(AuthFormEvent.PasswordChanged(it))
            },
            placeholderId = R.string.password,
            error = viewModel.state.passwordError
        )

        PasswordTextField(
            value = viewModel.state.repeatedPassword,
            onValueChange = {
                viewModel.onEvent(AuthFormEvent.RepeatedPasswordChanged(it))
            },
            placeholderId = R.string.confirm_password,
            error = viewModel.state.repeatedPasswordError
        )

        WideButton(
            stringId = R.string.login,
            onClick = {
                viewModel.onEvent(AuthFormEvent.Submit(AuthType.Register))
            }
        )
    }
}