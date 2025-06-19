package com.menene.trainspot.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menene.trainspot.home.data.TrainStorage
import com.menene.trainspot.home.domain.FirebaseTrainRepository
import com.menene.trainspot.util.Result
import kotlinx.coroutines.launch

class AddTrainViewModel(
    private val firebaseTrainRepository: FirebaseTrainRepository,
    private val trainStorage: TrainStorage
): ViewModel() {
    fun fetchTrainNames(){
        viewModelScope.launch {
            val list = firebaseTrainRepository.getTrainNames()
            trainStorage.setList((list as Result.Success).data)
        }
    }

    fun getTrainNames() = trainStorage.getTrainNames()

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