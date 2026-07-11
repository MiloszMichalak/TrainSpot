package pl.meleko.trainspot.presentation.username

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
import pl.meleko.trainspot.domain.repository.UserRepository
import pl.meleko.trainspot.presentation.util.toUiText
import pl.meleko.trainspot.requests.UpdateProfileRequest
import kotlin.random.Random

class UsernameViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UsernameState())
    val state = _state.asStateFlow()

    private val _events = Channel<UsernameEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: UsernameAction) {
        when(action) {
            is UsernameAction.OnUsernameChange -> _state.update { it.copy(username = action.username, isAvailable = null, error = null) }
            UsernameAction.OnSubmit -> submitUsername()
            UsernameAction.OnSkip -> skipUsername()
        }
    }

    private fun checkUsername(username: String) {
        if (username.isBlank()) return

        viewModelScope.launch {
            userRepository.checkUsernameAvailability(username)
                .onSuccess { available ->
                    _state.update { it.copy(isAvailable = available) }
                }
        }
    }

    private fun submitUsername() {
        val username = state.value.username
        if (username.isBlank()) return

        checkUsername(username)

        if (_state.value.isAvailable == false) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            userRepository.updateProfile(UpdateProfileRequest(username = username))
                .onSuccess {
                    _events.send(UsernameEvent.Success)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    _events.send(UsernameEvent.Error(error.toUiText()))
                }
        }
    }

    private fun skipUsername() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val randomUsername = "User${Random.nextInt(1000, 9999)}"
            userRepository.updateProfile(UpdateProfileRequest(username = randomUsername))
                .onSuccess {
                    _events.send(UsernameEvent.Success)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    _events.send(UsernameEvent.Error(error.toUiText()))
                }
        }
    }
}
