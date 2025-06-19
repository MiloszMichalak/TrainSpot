package com.menene.trainspot.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menene.trainspot.home.domain.FirebaseTrainRepository
import kotlinx.coroutines.launch

class AddTrainViewModel(
    private val firebaseTrainRepository: FirebaseTrainRepository
): ViewModel() {
    fun addTrainImage(bytes: ByteArray) {
        viewModelScope.launch {
            firebaseTrainRepository.uploadTrainImage(bytes)
        }
    }

    fun getTrainImage(imageUri: String) {
        viewModelScope.launch {
            firebaseTrainRepository.getTrainImage(imageUri)
        }
    }
}