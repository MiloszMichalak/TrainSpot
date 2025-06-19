package com.menene.trainspot.home.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TrainStorage {
    private val _trainList = MutableStateFlow<List<String>>(emptyList())
    val trainList = _trainList.asStateFlow()

    fun setList(newList: List<String>){
        _trainList.value = newList
    }

    fun getTrainNames(): List<String> {
        return _trainList.value
    }
}