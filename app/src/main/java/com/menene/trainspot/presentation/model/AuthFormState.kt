package com.menene.trainspot.presentation.model

import com.menene.trainspot.domain.use_case.ValidateEmail.EmailError
import com.menene.trainspot.domain.use_case.ValidatePassword.PasswordError
import com.menene.trainspot.domain.use_case.ValidateRepeatedPassword.RepeatedPasswordError

data class AuthFormState (
    val email: String = "",
    var emailError: EmailError? = null,
    val password: String = "",
    var passwordError: PasswordError? = null,
    val repeatedPassword: String = "",
    var repeatedPasswordError: RepeatedPasswordError? = null,
    val hasError: Boolean = false,
)