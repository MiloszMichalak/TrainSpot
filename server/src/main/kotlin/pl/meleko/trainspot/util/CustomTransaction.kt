package pl.meleko.trainspot.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

suspend inline fun <T> dbTransaction(crossinline action: suspend () -> T): T {
    return withContext(Dispatchers.IO) {
        suspendTransaction {
            action()
        }
    }
}