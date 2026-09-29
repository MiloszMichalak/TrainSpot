package pl.meleko.trainspot.presentation.profile

import pl.meleko.trainspot.model.User

data class ProfileState(
    val user: User? = null,
    val username: String = "",
    val isLoading: Boolean = true,
    val isSavingUsername: Boolean = false,
    val isUploadingAvatar: Boolean = false
)
