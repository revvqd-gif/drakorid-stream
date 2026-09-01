package com.drakorid.stream.data.remote

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plain OkHttp client used for endpoints that don't go through Retrofit:
 *  - AJAX endpoints that return HTML fragments (carousels, comment threads, similar-dramas lists)
 *  - The token-fetcher (a vanilla GET of the homepage)
 *  - Server-side search (/cari.html?q=…)
 *  - Watch page (/watch-fast/.../...) used to extract per-quality iframe URLs
 *  - File resolver (http://admin.drakor.la/go/files/{id}) which 302s to skXX.drakor.cc
 */
@Singleton
class HtmlClient @Inject constructor(
    private val okHttp: OkHttpClient,
) {

    /** GET request with a Mozilla-style User-Agent. Returns raw body string. */
    suspend fun get(url: String): String =
        okHttp.newCall(
            Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0 Mobile Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()
        ).execute().use { resp ->
            resp.body?.string().orEmpty()
        }

    /**
     * POST application/x-www-form-urlencoded. Used by every `/ajax/*.php` and `/myapi/*.php`
     * that doesn't have a Retrofit-mapped DTO.
     */
    suspend fun postForm(url: String, fields: Map<String, String>): String {
        val body = FormBody.Builder().apply {
            for ((k, v) in fields) add(k, v)
        }.build()
        return okHttp.newCall(
            Request.Builder()
                .url(url)
                .post(body)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0 Mobile Safari/537.36")
                .header("Accept", "*/*")
                .header("X-Requested-With", "XMLHttpRequest")
                .build()
        ).execute().use { resp ->
            resp.body?.string().orEmpty()
        }
    }

    /**
     * Issues a request and follows redirects, returning the final URL (used to
     * resolve `admin.drakor.la/go/files/{id}` → `skXX.drakor.cc/files/...mp4`).
     */
    suspend fun followRedirects(url: String): String =
        okHttp.newCall(
            Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0 Mobile Safari/537.36")
                .build()
        ).execute().use { resp ->
            // Read & close body to avoid connection leak; we only need the resolved URL.
            resp.body?.close()
            resp.request.url.toString()
        }
}