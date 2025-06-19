package com.menene.trainspot.home.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import com.menene.trainspot.R
import com.menene.trainspot.auth.presentation.components.WideButton
import com.menene.trainspot.home.util.ImageCompressor
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddTrainScreen(
    modifier: Modifier = Modifier,
    addTrainViewModel: AddTrainViewModel = koinViewModel(),
    imageCompressor: ImageCompressor = koinInject(),
) {
    var compressedImage by remember { mutableStateOf<ByteArray?>(null) }

    val scope = rememberCoroutineScope()

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        scope.launch {
           compressedImage = imageCompressor.compressImage(
                imageUri = uri,
                compressionThreshold = 200 * 1024L // 200 KB
            )
        }
    }

    Column {
        AsyncImage(
            model = compressedImage,
            contentDescription = "train",
        )

        WideButton(
            stringId = R.string.add_train,
            onClick = {
                pickPhoto.launch(
                    PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
        )

        WideButton(
            stringId = R.string.upload,
            onClick = {
                compressedImage?.let { bytes ->
                    addTrainViewModel.addTrainImage(bytes)
                }
            }
        )
    }
}