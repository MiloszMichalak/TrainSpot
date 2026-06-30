package pl.meleko.trainspot.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.requests.LoginRequest
import pl.meleko.trainspot.requests.RegisterRequest
import pl.meleko.trainspot.response.AuthResponse

class AuthService(private val client: HttpClient) {
    suspend fun login(email: String, password: String): Result<AuthResponse, DataError.Network> {
        return safeCall {
            client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(emailOrUsername = email, password = password))
            }
        }
    }

    suspend fun register(email: String, password: String): Result<AuthResponse, DataError.Network> {
        return safeCall {
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email = email, password = password))
            }
        }
    }
}
