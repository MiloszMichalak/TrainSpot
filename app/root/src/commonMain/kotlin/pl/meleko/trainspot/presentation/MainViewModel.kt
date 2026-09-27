package pl.meleko.trainspot.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.onFailure
import pl.meleko.trainspot.core.onSuccess
import pl.meleko.trainspot.domain.AuthRepository
import pl.meleko.trainspot.domain.DictionaryRepository

sealed interface AuthState {
    data object Loading : AuthState
    data object Authenticated : AuthState
    data object Unauthenticated : AuthState
}

class MainViewModel(
    private val authRepository: AuthRepository,
    private val dictionaryRepository: DictionaryRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            val token = authRepository.getToken()
            if (token != null) {
                authRepository.checkSession()
                    .onSuccess {
                        _authState.value = AuthState.Authenticated
                        dictionaryRepository.syncDictionariesIfNeeded()
                    }
                    .onFailure { error ->
                        if (error == DataError.Network.UNAUTHORIZED) {
                            authRepository.refreshTokens()
                                .onSuccess {
                                    _authState.value = AuthState.Authenticated
                                    dictionaryRepository.syncDictionariesIfNeeded()
                                }
                                .onFailure {
                                    _authState.value = AuthState.Unauthenticated
                                    viewModelScope.launch {
                                        authRepository.logout()
                                    }
                                }
                        } else {
                            _authState.value = AuthState.Unauthenticated
                            viewModelScope.launch {
                                authRepository.logout()
                            }
                        }
                    }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
}
