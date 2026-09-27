package pl.meleko.trainspot.presentation.register

sealed interface RegisterAction {
    data class OnEmailChange(val email: String): RegisterAction
    data class OnPasswordChange(val password: String): RegisterAction
    data class OnRepeatPasswordChange(val password: String): RegisterAction
    data object OnTogglePasswordVisibility: RegisterAction
    data object OnRegisterClick: RegisterAction
    data object OnLoginClick: RegisterAction
}
