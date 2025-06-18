package com.menene.trainspot.home.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileManager(
    private val context: Context
) {
    suspend fun saveImage(
        imageUri: Uri,
        filename: String
    ) {
        withContext(Dispatchers.IO) {
            context
                .contentResolver
                .openInputStream(imageUri)
                ?.use { inputStream ->
                    context
                        .openFileOutput(filename, Context.MODE_PRIVATE)
                        .use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                }
        }
    }


    suspend fun saveImage(
        bytes: ByteArray,
        fileName: String
    ) {
        withContext(Dispatchers.IO) {
            context
                .openFileOutput(fileName, Context.MODE_PRIVATE)
                .use { outputStream ->
                    outputStream.write(bytes)
                }
        }
    }
}