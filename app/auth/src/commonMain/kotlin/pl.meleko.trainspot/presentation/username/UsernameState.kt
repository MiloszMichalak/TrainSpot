package pl.meleko.trainspot.presentation.username

data class UsernameState(
    val username: String = "",
    val isAvailable: Boolean? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
