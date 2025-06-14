package com.menene.trainspot.presentation.model

sealed interface AuthType {
    data object Login : AuthType
    data object Register : AuthType
}