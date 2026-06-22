package pl.meleko.trainspot.util

import org.mindrot.jbcrypt.BCrypt

object Bcrypt {
    fun String.hashPassword(): String =
        BCrypt.hashpw(this, BCrypt.gensalt())

    fun String.checkPassword(plain: String): Boolean =
        BCrypt.checkpw(plain, this)
}