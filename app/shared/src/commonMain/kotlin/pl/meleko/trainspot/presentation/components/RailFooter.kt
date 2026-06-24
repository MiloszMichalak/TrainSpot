package pl.meleko.trainspot.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RailFooter(
    text: String,
    actionText: String,
    onActionClick: () -> Unit,
    version: String? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = colorScheme.outline)) {
            append(text)
        }
        withStyle(style = SpanStyle(color = colorScheme.primary)) {
            append(actionText)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = annotatedString,
            modifier = Modifier.clickable { onActionClick() },
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 12.sp
            )
        )
        
        if (version != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = version,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colorScheme.outline.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
