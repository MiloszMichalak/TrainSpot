package pl.meleko.trainspot.presentation.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    data class DynamicString(val value: String): UiText
    class ResString(
        val resource: StringResource,
        val args: Array<Any> = emptyArray()
    ): UiText

    @Composable
    fun asString(): String {
        return when(this) {
            is DynamicString -> value
            is ResString -> stringResource(resource, *args)
        }
    }
}
