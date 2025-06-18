package com.menene.trainspot.home.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import com.menene.trainspot.R
import com.menene.trainspot.auth.presentation.components.WideButton

@Composable
fun AddTrainScreen(modifier: Modifier = Modifier) {
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            photoUri = uri
        } else {
            // jakis snackbar, ze nie wybrano zdjecia
        }
    }

    AsyncImage(
        model = photoUri,
        contentDescription = "train",
    )


    WideButton(
        stringId = R.string.add_train,
        onClick = {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
    )
}