package com.menene.trainspot

import com.menene.trainspot.auth.domain.use_case.ValidateEmail
import com.menene.trainspot.util.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ValidateEmailTest {
    private val validEmail = ValidateEmail()

    @Test
    fun `empty email returns error EMPTY`() {
        val result = validEmail("")
        assertTrue(result is Result.Error)
        assertEquals(ValidateEmail.EmailError.EMPTY, (result as Result.Error).error)
    }

    @Test
    fun `invalid email returns error INVALID_EMAIL`() {
        val result = validEmail("sigmabimba.com")
        assertTrue(result is Result.Error)
        assertEquals(ValidateEmail.EmailError.INVALID_EMAIL, (result as Result.Error).error)
    }

    @Test
    fun `valid email returns success`() {
        val result = validEmail("test@example.com")
        assertTrue(result is Result.Success)
    }
}