package com.menene.trainspot.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menene.trainspot.domain.repository.AuthRepository
import com.menene.trainspot.domain.use_case.ValidateEmail
import com.menene.trainspot.domain.use_case.ValidatePassword
import com.menene.trainspot.domain.use_case.ValidateRepeatedPassword
import com.menene.trainspot.presentation.model.AuthFormEvent
import com.menene.trainspot.presentation.model.AuthFormState
import com.menene.trainspot.presentation.model.AuthType
import com.menene.trainspot.util.Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val validateEmail: ValidateEmail,
    private val validatePassword: ValidatePassword,
    private val validateRepeatedPassword: ValidateRepeatedPassword

): ViewModel() {
    var state by mutableStateOf(AuthFormState())

    private val _authEventChannel = Channel<AuthEvent>()
    val validationEvent = _authEventChannel.receiveAsFlow()

    fun onEvent(event: AuthFormEvent){
        when (event) {
            is AuthFormEvent.EmailChanged -> {
                state = state.copy(
                    email = event.email,
                    emailError = null,
                    hasError = false
                )
            }
            is AuthFormEvent.PasswordChanged -> {
                state = state.copy(
                    password = event.password,
                    passwordError = null,
                    hasError = false
                )
            }
            is AuthFormEvent.RepeatedPasswordChanged -> {
                state = state.copy(
                    repeatedPassword = event.repeatedPassword,
                    repeatedPasswordError = null,
                    hasError = false
                )
            }
            is AuthFormEvent.Submit -> submitData(event.registrationType)
        }
    }

    private fun submitData(registrationType: AuthType) {
        val emailResult = validateEmail.invoke(state.email)
        val passwordResult = validatePassword.invoke(state.password)
        val repeatedPasswordResult = if (registrationType == AuthType.Register) {
            validateRepeatedPassword.invoke(state.repeatedPassword, state.password)
        } else {
            Result.Success(Unit)
        }

        val hasError = listOf(
            emailResult,
            passwordResult,
            repeatedPasswordResult
        ).any { it is Result.Error}

        if (hasError) {
            state = state.copy(
                emailError = if (emailResult is Result.Error) emailResult.error else null,
                passwordError = if (passwordResult is Result.Error) passwordResult.error else null,
                repeatedPasswordError = if (repeatedPasswordResult is Result.Error) repeatedPasswordResult.error else null,
                hasError = true
            )
            return
        }

        viewModelScope.launch {
            val result = when (registrationType) {
                AuthType.Register -> authRepository.register(state.email, state.password)
                AuthType.Login -> authRepository.login(state.email, state.password)
            }

            if (result is Result.Success) {
                _authEventChannel.send(AuthEvent.Success)
            } else {
                _authEventChannel.send(AuthEvent.Failure)
            }
        }
    }

    sealed class AuthEvent {
        data object Success : AuthEvent()
        data object Failure : AuthEvent()
    }
}