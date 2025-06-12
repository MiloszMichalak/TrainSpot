package com.menene.trainspot.util

typealias DomainError = Error

sealed interface Result<out D, out E: DomainError> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: DomainError>(val error: E): Result<Nothing, E>
}