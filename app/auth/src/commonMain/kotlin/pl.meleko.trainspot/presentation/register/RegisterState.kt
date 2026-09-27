package pl.meleko.trainspot.presentation.register

import pl.meleko.trainspot.presentation.util.UiText

data class RegisterState(
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: UiText? = null
)
