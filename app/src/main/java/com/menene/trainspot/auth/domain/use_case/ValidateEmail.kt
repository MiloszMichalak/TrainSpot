package com.menene.trainspot.auth.domain.use_case

import com.menene.trainspot.R
import com.menene.trainspot.util.Error
import com.menene.trainspot.util.Result

class ValidateEmail {
    companion object {
        private val emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
    }

    operator fun invoke(email: String): Result<String, EmailError> {
        if (email.isEmpty()) {
            return Result.Error(EmailError.EMPTY)
        }

        if (!emailPattern.matches(email)){
            return Result.Error(EmailError.INVALID_EMAIL)
        }

        return Result.Success(email)
    }


    enum class EmailError : Error {
        EMPTY,
        INVALID_EMAIL;

        override fun toIntType(): Int {
            return  when (this) {
                EMPTY -> R.string.email_not_empty
                INVALID_EMAIL -> R.string.invalid_email
            }
        }
    }


}