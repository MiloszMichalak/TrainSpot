package pl.meleko.trainspot.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
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

    suspend fun createSpot(request: SpotRequest): Result<Spot, DataError.Network> {
        return safeCall {
            client.post("/spot") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    suspend fun updateSpot(id: String, request: SpotRequest): Result<Spot, DataError.Network> {
        return safeCall {
            client.put("/spot/$id") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    suspend fun updateSpotImage(
        id: String,
        image: ByteArray,
        fileName: String = "spot.jpg",
        mimeType: String = "image/jpeg"
    ): Result<Spot, DataError.Network> {
        return safeCall {
            client.put("/spot/$id/image") {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("image", image, Headers.build {
                            val safeFileName = fileName.substringAfterLast('/').substringAfterLast('\\')
                                .filter { it.isLetterOrDigit() || it == '.' || it == '-' || it == '_' }
                            append(HttpHeaders.ContentDisposition, "filename=\"${safeFileName.ifBlank { "spot.jpg" }}\"")
                            append(HttpHeaders.ContentType, mimeType)
                        })
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
