package pl.meleko.trainspot.presentation.profile

sealed interface ProfileAction {
    data class UsernameChanged(val value: String) : ProfileAction
    data class AvatarSelected(val bytes: ByteArray, val fileName: String, val contentType: String) : ProfileAction
    data object SaveUsername : ProfileAction
    data object AvatarPickFailed : ProfileAction
}
