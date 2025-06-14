package com.menene.trainspot.domain.use_case

import android.util.Patterns
import com.menene.trainspot.R
import com.menene.trainspot.util.Error
import com.menene.trainspot.util.Result
import java.util.regex.Pattern

class ValidateEmail {
    operator fun invoke(email: String): Result<String, EmailError> {
        if (email.isEmpty()) {
            return Result.Error(EmailError.EMPTY)
        }

        if (!Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(), email)){
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