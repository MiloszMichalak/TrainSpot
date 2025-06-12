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
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel()
) {
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
        )

        PasswordTextField(
            value = viewModel.state.password,
            placeholderId = R.string.password,
            onValueChange = {
                viewModel.onEvent(AuthFormEvent.PasswordChanged(it))
            }
        )

        WideButton(
            stringId = R.string.login,
            onClick = {}
        )
    }
}