package pl.meleko.trainspot.plugins

import io.ktor.server.application.createApplicationPlugin
import kotlinx.coroutines.launch
import pl.meleko.trainspot.repository.SessionsRepository
import pl.meleko.trainspot.util.jwtSessionId

val SessionTracker = createApplicationPlugin(name = "SessionTracker") {
    onCall { call ->
        val sessionId = try {
            call.jwtSessionId()
        } catch (e: Exception) {
            null
        }

        if (sessionId != null) {
            call.application.launch {
                SessionsRepository.updateLastSeen(sessionId)
            }
        }
    }
}
