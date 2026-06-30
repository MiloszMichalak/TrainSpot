package pl.meleko.trainspot.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.append
import kotlinx.serialization.json.Json
import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.model.Spot
import pl.meleko.trainspot.requests.SpotRequest
import pl.meleko.trainspot.response.PaginationResponse

class SpotService(private val client: HttpClient) {
    suspend fun getSpots(page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network> {
        return safeCall {
            client.get("/spot") {
                parameter("page", page)
                parameter("limit", limit)
            }
        }
    }

    suspend fun getSpot(id: String): Result<Spot, DataError.Network> {
        return safeCall {
            client.get("/spot/$id")
        }
    }

    suspend fun createSpot(request: SpotRequest, image: ByteArray): Result<Spot, DataError.Network> {
        return safeCall {
            client.submitFormWithBinaryData(
                url = "/spot",
                formData = formData {
                    append("request", Json.encodeToString(request), Headers.build {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    })
                    append("image", image, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"spot.jpg\"")
                        append(HttpHeaders.ContentType, "image/jpeg")
                    })
                }
            )
        }
    }

    suspend fun updateSpot(id: String, request: SpotRequest, image: ByteArray?): Result<Spot, DataError.Network> {
        return safeCall {
            client.put("/spot/$id") {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("request", Json.encodeToString(request), Headers.build {
                            append(HttpHeaders.ContentType, ContentType.Application.Json)
                        })
                        if (image != null) {
                            append("image", image, Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"spot.jpg\"")
                                append(HttpHeaders.ContentType, "image/jpeg")
                            })
                        }
                    }
                ))
            }
        }
    }

    suspend fun deleteSpot(id: String): Result<Unit, DataError.Network> {
        return safeCall {
            client.delete("/spot/$id")
        }
    }

    suspend fun getUserSpots(userId: String, page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network> {
        return safeCall {
            client.get("/spot/user/$userId") {
                parameter("page", page)
                parameter("limit", limit)
            }
        }
    }

    suspend fun getMySpots(page: Int, limit: Int): Result<PaginationResponse<Spot>, DataError.Network> {
        return safeCall {
            client.get("/spot/user/me") {
                parameter("page", page)
                parameter("limit", limit)
            }
        }
    }
}
