package com.drakorid.stream.data.remote

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenProvider @Inject constructor(
    private val client: OkHttpClient,
) {
    private val mutex = Mutex()
    @Volatile private var cached: String? = null
    @Volatile private var expiresAt: Long = 0L

    suspend fun get(): String = mutex.withLock {
        val now = System.currentTimeMillis()
        cached?.takeIf { now < expiresAt }?.let { return@withLock it }

        val html = fetchHomepage()
        val token = TOKEN_REGEX.find(html)?.groupValues?.get(1)
            ?: error("token_now not found on homepage")

        cached = token
        expiresAt = now + TOKEN_TTL_MS
        token
    }

    suspend fun invalidate() = mutex.withLock {
        cached = null
        expiresAt = 0L
    }

    private fun fetchHomepage(): String = client.newCall(
        Request.Builder()
            .url("https://drakorid.co/")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
            .build()
    ).execute().use { it.body?.string().orEmpty() }

    companion object {
        private const val TOKEN_TTL_MS = 10 * 60 * 1000L
        private val TOKEN_REGEX = Regex("""token_now\s*=\s*"([^"]+)"""")
    }
}