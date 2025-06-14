package com.menene.trainspot.auth.presentation.model

sealed interface AuthType {
    data object Login : AuthType
    data object Register : AuthType
}