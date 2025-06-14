package com.menene.trainspot.presentation.model

sealed class AuthFormEvent {
    data class EmailChanged(val email: String) : AuthFormEvent()
    data class PasswordChanged(val password: String) : AuthFormEvent()
    data class RepeatedPasswordChanged(val repeatedPassword: String) : AuthFormEvent()

    data class Submit(val registrationType: AuthType) : AuthFormEvent()
}