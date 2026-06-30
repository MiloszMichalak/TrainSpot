package pl.meleko.trainspot.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.meleko.trainspot.domain.repository.AuthRepository
import pl.meleko.trainspot.domain.repository.DictionaryRepository

class MainViewModel(
    private val authRepository: AuthRepository,
    private val dictionaryRepository: DictionaryRepository
) : ViewModel() {

    init {
        checkAndSync()
    }

    private fun checkAndSync() {
        viewModelScope.launch {
            if (authRepository.getToken() != null) {
                dictionaryRepository.syncDictionariesIfNeeded()
            }
        }
    }

    fun syncAfterLogin() {
        viewModelScope.launch {
            dictionaryRepository.syncDictionariesIfNeeded()
        }
    }
}
