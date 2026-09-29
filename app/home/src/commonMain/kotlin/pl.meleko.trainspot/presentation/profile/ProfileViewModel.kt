package pl.meleko.trainspot.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.launch
import pl.meleko.trainspot.core.onFailure
import pl.meleko.trainspot.core.onSuccess
import pl.meleko.trainspot.domain.UserRepository
import trainspot.app.shared.generated.resources.Res
import trainspot.app.shared.generated.resources.error_media_file
import trainspot.app.shared.generated.resources.profile_saved
import pl.meleko.trainspot.presentation.util.UiText
import pl.meleko.trainspot.presentation.util.toUiText
import pl.meleko.trainspot.requests.UpdateProfileRequest

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val usernameSaveMutex = Mutex()
    private val avatarUploadMutex = Mutex()
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            userRepository.myProfile.collect { user ->
                if (user == null) return@collect
                _state.update { current ->
                    val usernameChangedByServer = current.user?.username != user.username
                    current.copy(
                        user = user,
                        username = if (current.user == null || usernameChangedByServer) user.username.orEmpty() else current.username,
                        isLoading = false
                    )
                }
            }
        }
        viewModelScope.launch {
            userRepository.getMyProfile().onFailure { error ->
                _state.update { it.copy(isLoading = false) }
                _events.send(ProfileEvent.Error(error.toUiText()))
            }
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.UsernameChanged -> _state.update { it.copy(username = action.value) }
            ProfileAction.SaveUsername -> saveUsername()
            is ProfileAction.AvatarSelected -> uploadAvatar(action)
            ProfileAction.AvatarPickFailed -> viewModelScope.launch {
                _events.send(ProfileEvent.Error(UiText.ResString(Res.string.error_media_file)))
            }
        }
    }

    private fun saveUsername() {
        val username = _state.value.username.trim()
        if (username.isEmpty() || username.length > 100 || username == _state.value.user?.username) return
        if (!usernameSaveMutex.tryLock()) return

        _state.update { it.copy(isSavingUsername = true) }
        viewModelScope.launch {
            try {
                userRepository.updateProfile(UpdateProfileRequest(username = username))
                    .onSuccess { _events.send(ProfileEvent.Saved(UiText.ResString(Res.string.profile_saved))) }
                    .onFailure { _events.send(ProfileEvent.Error(it.toUiText())) }
            } finally {
                _state.update { it.copy(isSavingUsername = false) }
                usernameSaveMutex.unlock()
            }
        }
    }

    private fun uploadAvatar(action: ProfileAction.AvatarSelected) {
        if (!avatarUploadMutex.tryLock()) return
        _state.update { it.copy(isUploadingAvatar = true) }
        viewModelScope.launch {
            try {
                userRepository.updateAvatar(action.bytes, action.fileName, action.contentType)
                    .onSuccess { _events.send(ProfileEvent.Saved(UiText.ResString(Res.string.profile_saved))) }
                    .onFailure { _events.send(ProfileEvent.Error(it.toUiText())) }
            } finally {
                _state.update { it.copy(isUploadingAvatar = false) }
                avatarUploadMutex.unlock()
            }
        }
    }
}
