package com.menene.trainspot.presentation.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.menene.trainspot.R
import com.menene.trainspot.domain.use_case.ValidateEmail.EmailError

@Composable
fun EmailTextField(
    value: String,
    error: EmailError?,
    onValueChange: (String) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = { onValueChange(it) },
        keyboardOptions = KeyboardOptions.Default.copy(
            autoCorrectEnabled = true,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Default,
            capitalization = KeyboardCapitalization.None
        ),
        placeholder = { Text(stringResource(R.string.email)) },
        singleLine = true,
        supportingText = { if (error != null) {
            Text(
                text = stringResource(error.toIntType()),
                color = MaterialTheme.colorScheme.error
            )
        } },
        trailingIcon = { if (error != null) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = stringResource(R.string.error_icon),
                tint = MaterialTheme.colorScheme.error
            )}
        }
    )
}