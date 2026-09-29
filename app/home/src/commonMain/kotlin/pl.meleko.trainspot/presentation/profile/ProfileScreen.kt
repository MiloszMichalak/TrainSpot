package pl.meleko.trainspot.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pl.meleko.trainspot.presentation.components.PrimaryButton
import pl.meleko.trainspot.presentation.components.RailTextField
import pl.meleko.trainspot.presentation.util.ObserveAsEvents
import pl.meleko.trainspot.presentation.util.SnackbarController
import trainspot.app.shared.generated.resources.Res
import trainspot.app.shared.generated.resources.profile_change_photo
import trainspot.app.shared.generated.resources.profile_change_username
import trainspot.app.shared.generated.resources.profile_title
import trainspot.app.shared.generated.resources.username_label
import trainspot.app.shared.generated.resources.username_placeholder

@Composable
fun ProfileRoot(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProfileEvent.Error -> SnackbarController.onEvent(event.message.asText())
            is ProfileEvent.Saved -> SnackbarController.onEvent(event.message.asText())
        }
    }
    ProfileScreen(state, viewModel::onAction, onNavigateBack)
}

@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    val pickerScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(end = 16.dp)) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color(0xFFF0EDE8))
                }
                Text(stringResource(Res.string.profile_title), color = Color(0xFFF0EDE8))
            }
        },
        containerColor = Color(0xFF0D0E0F)
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE8C547))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier.size(128.dp).clip(CircleShape).background(Color(0xFF1F2124)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.user?.avatarUrl != null) {
                            AsyncImage(
                                model = state.user.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(52.dp), tint = Color(0xFFE8C547))
                        }
                    }
                    PrimaryButton(
                        text = stringResource(Res.string.profile_change_photo),
                        onClick = {
                            pickerScope.launch {
                                try {
                                    val file = FileKit.openFilePicker(type = FileKitType.Image) ?: return@launch
                                    val mimeType = file.mimeType()?.toString() ?: "image/jpeg"
                                    if (mimeType !in setOf("image/jpeg", "image/png", "image/webp")) {
                                        onAction(ProfileAction.AvatarPickFailed)
                                        return@launch
                                    }
                                    val extension = mimeType.substringAfter('/').substringBefore('+').take(8)
                                    val bytes = file.readBytes()
                                    if (bytes.size > 5 * 1024 * 1024) {
                                        onAction(ProfileAction.AvatarPickFailed)
                                    } else {
                                        onAction(ProfileAction.AvatarSelected(bytes, "avatar.$extension", mimeType))
                                    }
                                } catch (cancel: CancellationException) {
                                    throw cancel
                                } catch (_: Throwable) {
                                    onAction(ProfileAction.AvatarPickFailed)
                                }
                            }
                        },
                        isLoading = state.isUploadingAvatar,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(32.dp))
                RailTextField(
                    label = stringResource(Res.string.username_label),
                    value = state.username,
                    onValueChange = { onAction(ProfileAction.UsernameChanged(it)) },
                    placeholder = stringResource(Res.string.username_placeholder),
                    icon = Icons.Default.Person
                )
                Spacer(Modifier.height(14.dp))
                PrimaryButton(
                    text = stringResource(Res.string.profile_change_username),
                    onClick = { onAction(ProfileAction.SaveUsername) },
                    isLoading = state.isSavingUsername,
                    enabled = state.username.trim().isNotEmpty() && state.username.trim().length <= 100 &&
                        state.username.trim() != state.user?.username
                )
            }
        }
    }
}
