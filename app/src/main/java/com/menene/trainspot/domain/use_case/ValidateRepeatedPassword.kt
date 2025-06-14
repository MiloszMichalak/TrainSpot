package com.menene.trainspot.domain.use_case

import com.menene.trainspot.util.Error
import com.menene.trainspot.util.Result

class ValidateRepeatedPassword {
    operator fun invoke(password: String, repeatedPassword: String) : Result<String, RepeatedPasswordError> {
        if (password != repeatedPassword){
            return Result.Error(RepeatedPasswordError.PASSWORD_DO_NOT_MATCH)
        }

        return Result.Success(repeatedPassword)
    }



    enum class RepeatedPasswordError : Error {
        PASSWORD_DO_NOT_MATCH;

        override fun toIntType(): Int {
            return when (this) {
                PASSWORD_DO_NOT_MATCH -> com.menene.trainspot.R.string.password_do_not_match
            }
        }
    }
}