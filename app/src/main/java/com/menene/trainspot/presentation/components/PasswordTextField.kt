package com.menene.trainspot.presentation.components

import androidx.annotation.StringRes
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
import com.menene.trainspot.domain.model.PasswordErrorFunction

@Composable
fun PasswordTextField(
    value: String,
    @StringRes placeholderId: Int,
    error: PasswordErrorFunction?,
    onValueChange: (String) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = { onValueChange(it) },
        keyboardOptions = KeyboardOptions.Default.copy(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Password,
            capitalization = KeyboardCapitalization.None,
            imeAction = ImeAction.Default
        ),
        placeholder = { Text(stringResource(placeholderId)) },
        singleLine = true,
        supportingText = { if (error != null) {
                Text(
                    text = stringResource(error.toIntType()),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        trailingIcon = {
            if (error != null) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = stringResource(R.string.error_icon),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}