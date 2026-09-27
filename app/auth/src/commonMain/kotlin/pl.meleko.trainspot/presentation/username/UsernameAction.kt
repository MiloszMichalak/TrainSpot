package pl.meleko.trainspot.presentation.username

sealed interface UsernameAction {
    data class OnUsernameChange(val username: String) : UsernameAction
    object OnSubmit : UsernameAction
    object OnSkip : UsernameAction
}
