package pl.meleko.trainspot.presentation.register

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
import pl.meleko.trainspot.presentation.util.UiText
import pl.meleko.trainspot.presentation.util.toUiText

class RegisterViewModel(
    private val authRepository: AuthRepository
): ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    private val _events = Channel<RegisterEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: RegisterAction) {
        when(action) {
            is RegisterAction.OnEmailChange -> {
                _state.update { it.copy(email = action.email) }
            }
            is RegisterAction.OnPasswordChange -> {
                _state.update { it.copy(password = action.password) }
            }
            is RegisterAction.OnRepeatPasswordChange -> {
                _state.update { it.copy(repeatPassword = action.password) }
            }
            RegisterAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            RegisterAction.OnRegisterClick -> register()
            RegisterAction.OnLoginClick -> { /* Handled in Root */ }
        }
    }

    private fun register() {
        if(state.value.password != state.value.repeatPassword) {
            viewModelScope.launch {
                val errorMsg = UiText.DynamicString("Passwords do not match")
                _state.update { it.copy(error = errorMsg) }
                _events.send(RegisterEvent.Error(errorMsg))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            authRepository.register(
                email = state.value.email,
                password = state.value.password
            ).onSuccess {
                _state.update { it.copy(isLoading = false) }
                _events.send(RegisterEvent.RegisterSuccess)
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                _events.send(RegisterEvent.Error(error.toUiText()))
            }
        }
    }
}
