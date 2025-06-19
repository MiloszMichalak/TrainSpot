package com.menene.trainspot.home.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.menene.trainspot.home.presentation.components.TrainSearchBox
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchNameTrainScreen(
    addTrainViewModel: AddTrainViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        addTrainViewModel.fetchTrainNames()
    }

    val searchText = TextFieldState()
    val searchResults = addTrainViewModel.getTrainNames()

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        TrainSearchBox(
            textFieldState = searchText,
            onSearch = { searchText.edit { replace(0, length, it) } },
            searchResults = searchResults
        )
    }
}