package pl.meleko.trainspot.service

import io.ktor.http.HttpStatusCode
import pl.meleko.trainspot.database.model.SessionDto
import pl.meleko.trainspot.repository.SessionsRepository
import pl.meleko.trainspot.util.JwtUtil
import pl.meleko.trainspot.util.NetworkResult
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object SessionService {
    fun createSession(userId: Uuid): NetworkResult<String> {
        val session = SessionsRepository.create(userId)
        val token = JwtUtil.createToken(userId)
        return NetworkResult.Success(token)
    }

    fun updateLastSeen(sessionId: Uuid): NetworkResult<Unit> {
        val updated = SessionsRepository.updateLastSeen(sessionId)
        return if (updated) NetworkResult.Success(Unit) else NetworkResult.Error(HttpStatusCode.NotFound)
    }

    fun deleteSession(sessionId: Uuid): NetworkResult<Unit> {
        val deleted = SessionsRepository.delete(sessionId)
        return if (deleted) NetworkResult.Success(Unit) else NetworkResult.Error(HttpStatusCode.NotFound)
    }

    fun getSession(sessionId: Uuid): NetworkResult<SessionDto> {
        val session = SessionsRepository.findById(sessionId)
            ?: return NetworkResult.Error(HttpStatusCode.NotFound)
        return NetworkResult.Success(session)
    }

    fun getUserSessions(userId: Uuid): NetworkResult<List<SessionDto>> {
        val sessions = SessionsRepository.findAllByUserId(userId)
        return NetworkResult.Success(sessions)
    }
}