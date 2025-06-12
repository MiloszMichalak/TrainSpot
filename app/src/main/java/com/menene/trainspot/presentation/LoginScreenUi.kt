package com.menene.trainspot.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.menene.trainspot.R
import com.menene.trainspot.presentation.components.EmailTextField
import com.menene.trainspot.presentation.components.PasswordTextField
import com.menene.trainspot.presentation.components.WideButton

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        var email by remember { mutableStateOf("") }
        val password by remember { mutableStateOf("") }

        Text(
            text = stringResource(R.string.login),
            style = MaterialTheme.typography.headlineLarge
        )

        EmailTextField(
            value = email,
            onValueChange = { },
        )

        PasswordTextField(
            value = password,
            placeholderId = R.string.password,
            onValueChange = { }
        )

        WideButton(
            stringId = R.string.login,
            onClick = {}
        )
    }
}