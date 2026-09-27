package pl.meleko.trainspot.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.meleko.trainspot.core.onFailure
import pl.meleko.trainspot.core.onSuccess
import pl.meleko.trainspot.domain.AuthRepository
import pl.meleko.trainspot.presentation.util.toUiText

class LoginViewModel(
    private val authRepository: AuthRepository
): ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when(action) {
            is LoginAction.OnEmailChange -> {
                _state.update { it.copy(email = action.email) }
            }
            is LoginAction.OnPasswordChange -> {
                _state.update { it.copy(password = action.password) }
            }
            LoginAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            LoginAction.OnLoginClick -> login()
            LoginAction.OnRegisterClick -> { /* Handled in Root */ }
        }
    }

    private fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            authRepository.login(
                email = state.value.email,
                password = state.value.password
            ).onSuccess {
                _state.update { it.copy(isLoading = false) }
                _events.send(LoginEvent.LoginSuccess)
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                _events.send(LoginEvent.Error(error.toUiText()))
            }
        }
    }
}
