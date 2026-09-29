package pl.meleko.trainspot.util

import io.ktor.http.content.PartData
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.utils.io.readBuffer
import kotlinx.io.readByteArray
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class IncomingImage(
    val bytes: ByteArray,
    val originalFileName: String?
)

suspend fun ApplicationCall.receiveImageData(): IncomingImage? {
    val multipart = receiveMultipart().readPart() ?: return null

    return try {
        when (multipart) {
            is PartData.FileItem -> {
                val bytes = multipart.provider().readBuffer().readByteArray()
                IncomingImage(bytes = bytes, originalFileName = multipart.originalFileName)
            }
            else -> null
        }
    } finally {
        multipart.release()
    }
}

object ImageStorage {
    @OptIn(ExperimentalUuidApi::class)
    fun save(
        image: IncomingImage,
        subdir: String,
        id: Uuid,
        baseUrl: String = "https://trainspot.meleko.pl",
        basePath: String = "/var/www/trainspot"
    ): String {
        val extension = image.originalFileName?.substringAfterLast(".", "jpg")
            ?.lowercase()
            ?.takeIf { it.matches(Regex("[a-z0-9]{1,10}")) }
            ?: "jpg"
        val fileName = "${Uuid.generateV4()}.$extension"

        val dir = File("$basePath/$subdir/$id")
        check(dir.exists() || dir.mkdirs()) { "Unable to create image directory" }

        File(dir, fileName).writeBytes(image.bytes)

        return "$baseUrl/$subdir/$id/$fileName"
    }

    fun delete(imageUrl: String) {
        val relativePath = imageUrl.removePrefix("https://trainspot.meleko.pl/")
        File("/var/www/trainspot", relativePath).delete()
    }
}
