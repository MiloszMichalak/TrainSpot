package com.menene.trainspot

import com.google.common.truth.Truth.assertThat
import com.menene.trainspot.auth.domain.use_case.ValidateRepeatedPassword
import com.menene.trainspot.util.Result
import org.junit.Test

class ValidateRepeatedPasswordTest {
    val validateRepeatedPassword = ValidateRepeatedPassword()

    @Test
    fun `repeated password does not match original password returns error PASSWORD_DO_NOT_MATCH`() {
        val result = validateRepeatedPassword("Test123", "Test456")
        assertThat((result as Result.Error).error).isEqualTo(ValidateRepeatedPassword.RepeatedPasswordError.PASSWORD_DO_NOT_MATCH)
    }

    @Test
    fun `repeated password matches original password returns success`(){
        val result = validateRepeatedPassword("Test123", "Test123")
        assertThat(result).isInstanceOf(Result.Success::class.java)
    }
}