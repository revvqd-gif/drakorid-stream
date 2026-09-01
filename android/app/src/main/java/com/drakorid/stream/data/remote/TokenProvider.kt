package com.drakorid.stream.data.remote

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches and caches the per-render `token_now` value embedded in every drakorid.co
 * HTML page. Token is required by every AJAX endpoint (`/ajax/*.php`, `/myapi/*.php`).
 *
 * Lifetime is unknown but observed > 30 min across page loads. We refresh after
 * 10 min to be safe. On any "Token Not Found" failure we invalidate and fetch
 * fresh on the next call.
 */
@Singleton
class TokenProvider @Inject constructor(
    private val htmlClient: HtmlClient,
) {
    private val mutex = Mutex()
    @Volatile private var cached: String? = null
    @Volatile private var expiresAt: Long = 0L

    suspend fun get(): String = mutex.withLock {
        val now = System.currentTimeMillis()
        cached?.takeIf { now < expiresAt }?.let { return@withLock it }

        val html = htmlClient.get("https://drakorid.co/")
        val token = TOKEN_REGEX.find(html)?.groupValues?.get(1)
            ?: error("token_now not found on homepage")

        cached = token
        expiresAt = now + TOKEN_TTL_MS
        token
    }

    /** Invalidate after a server-side rejection. */
    suspend fun invalidate() = mutex.withLock {
        cached = null
        expiresAt = 0L
    }

    companion object {
        private const val TOKEN_TTL_MS = 10 * 60 * 1000L
        private val TOKEN_REGEX = Regex("""token_now\s*=\s*"([^"]+)"""")
    }
}