package pl.meleko.trainspot.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.User
import pl.meleko.trainspot.requests.UpdateProfileRequest

class UserService(private val client: HttpClient) {
    suspend fun getProfile(userId: String): Result<User, DataError.Network> {
        return safeCall {
            client.get("/users/$userId")
        }
    }

    suspend fun getMyProfile(): Result<User, DataError.Network> {
        return safeCall {
            client.get("/users/me")
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest, avatarBytes: ByteArray?): Result<User, DataError.Network> {
        return safeCall {
            client.put("/users/me") {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("request", Json.encodeToString(request), Headers.build {
                            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        })
                        if (avatarBytes != null) {
                            append("image", avatarBytes, Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"avatar.jpg\"")
                                append(HttpHeaders.ContentType, "image/jpeg")
                            })
                        }
                    }
                ))
            }
        }
    }

    suspend fun deleteProfile(): Result<Unit, DataError.Network> {
        return safeCall {
            client.delete("/users/me")
        }
    }

    suspend fun checkUsernameAvailability(username: String): Result<Boolean, DataError.Network> {
        return safeCall {
            client.get("/users/check-username") {
                parameter("username", username)
            }
        }
    }
}
