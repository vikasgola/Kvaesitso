package de.mm20.launcher2.rss

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RssRepository {

    private val httpClient by lazy {
        HttpClient {
            install(HttpTimeout) {
                connectTimeoutMillis = 10_000
                requestTimeoutMillis = 20_000
                socketTimeoutMillis = 20_000
            }
        }
    }

    suspend fun fetchFeed(feedUrl: String): Result<RssParsedFeed> = withContext(Dispatchers.IO) {
        try {
            val body = httpClient.get {
                url(normalizeUrl(feedUrl))
                headers {
                    append(
                        "User-Agent",
                        "KvaesitsoRSS/1.0 (Android; +https://github.com/MM2-0/Kvaesitso)",
                    )
                    append("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml, */*")
                }
            }.bodyAsText()
            val parsed = RssParser.parse(body)
                ?: return@withContext Result.failure(IllegalArgumentException("Unrecognized feed format"))
            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun normalizeUrl(raw: String): String {
        val t = raw.trim()
        if (t.startsWith("http://", ignoreCase = true) || t.startsWith("https://", ignoreCase = true)) {
            return t
        }
        return "https://$t"
    }
}
