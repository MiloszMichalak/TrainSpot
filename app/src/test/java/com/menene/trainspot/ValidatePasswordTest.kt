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

    @Test
    fun `password shorter than 6 characters returns error PASSWORD_TOO_SHORT`() {
        val result = validPassword("test")
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(ValidatePassword.PasswordError.PASSWORD_TOO_SHORT)
    }

    @Test
    fun `password without uppercase returns error NO_UPPERCASE`() {
        val result = validPassword("test123")
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(ValidatePassword.PasswordError.NO_UPPERCASE)
    }

    @Test
    fun `password without lowercase returns error NO_LOWERCASE`() {
        val result = validPassword("TEST123")
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(ValidatePassword.PasswordError.NO_LOWERCASE)
    }

    @Test
    fun `password without digit returns error NO_DIGIT`() {
        val result = validPassword("Testtest")
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(ValidatePassword.PasswordError.NO_DIGIT)
    }

    @Test
    fun `valid password returns success`() {
        val result = validPassword("Test123")
        assertThat(result).isInstanceOf(Result.Success::class.java)
    }
}