package pl.meleko.trainspot

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform