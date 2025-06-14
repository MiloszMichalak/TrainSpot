package com.menene.trainspot

import com.google.common.truth.Truth.assertThat
import com.menene.trainspot.domain.use_case.ValidatePassword
import com.menene.trainspot.util.Result
import org.junit.Test

class ValidatePasswordTest {
    private val validPassword = ValidatePassword()

    @Test
    fun `empty password returns error EMPTY`() {
        val result = validPassword("")
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(ValidatePassword.PasswordError.EMPTY)
    }
}