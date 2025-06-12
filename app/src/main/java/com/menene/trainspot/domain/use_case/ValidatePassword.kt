package com.menene.trainspot.domain.use_case

import com.menene.trainspot.util.Error
import com.menene.trainspot.util.Result

class ValidatePassword {
    operator fun invoke(password: String): Result<String, PasswordError> {
        if (password.isEmpty()) {
            return Result.Error(PasswordError.EMPTY)
        }

        if (password.length < 6) {
            return Result.Error(PasswordError.PASSWORD_TOO_SHORT)
        }

        if (!password.any { it.isUpperCase() }) {
            return Result.Error(PasswordError.NO_UPPERCASE)
        }

        if (!password.any { it.isLowerCase() }) {
            return Result.Error(PasswordError.NO_LOWERCASE)
        }

        if (!password.any { it.isDigit() }) {
            return Result.Error(PasswordError.NO_DIGIT)
        }

        return Result.Success(password)
    }


    enum class PasswordError : Error {
        EMPTY,
        PASSWORD_TOO_SHORT,
        NO_UPPERCASE,
        NO_LOWERCASE,
        NO_DIGIT
    }
}