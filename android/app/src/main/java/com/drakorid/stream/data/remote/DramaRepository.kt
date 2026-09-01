package com.drakorid.stream.data.remote

import com.drakorid.stream.domain.model.Drama
import org.jsoup.Jsoup
import com.drakorid.stream.domain.model.EpisodeEntry
import com.drakorid.stream.domain.model.EpisodeDetail
import com.drakorid.stream.domain.model.QualityOption
import com.drakorid.stream.domain.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository over the public drakorid.co web pages + JSON API.
 * All reads run on [Dispatchers.IO].
 */
@Singleton
class DramaRepository @Inject constructor(
    private val client: OkHttpClient,
    private val tokenProvider: TokenProvider,
) {

    // ------------------------------------------------------------------ //
    //  HTML pages (public)                                                 //
    // ------------------------------------------------------------------ //

    suspend fun fetchPopular(): List<Drama> = io {
        val doc = Jsoup.parse(get("https://drakorid.co/populer"))
        doc.select(".movie-list .poster, .movie-list .content").map { el ->
            Drama(
                id = (el.selectFirst("[data-id]")?.attr("data-id")?.toIntOrNull() ?: 0),
                slug = el.selectFirst("a[href*=drama]")?.attr("href")?.substringAfterLast("/").orEmpty(),
                title = el.selectFirst(".film-title, h2, h3")?.text().orEmpty(),
                posterUrl = el.selectFirst("img")?.attr("src").orEmpty(),
                rating = el.selectFirst(".rating")?.text().orEmpty(),
                isPremium = el.selectFirst(".premium") != null,
            )
        }.filter { it.slug.isNotEmpty() }
    }

    suspend fun fetchLatest(): List<Drama> = io {
        val doc = Jsoup.parse(get("https://drakorid.co/latest"))
        doc.select(".movie-list .poster, .movie-list .content").map { el ->
            Drama(
                id = (el.selectFirst("[data-id]")?.attr("data-id")?.toIntOrNull() ?: 0),
                slug = el.selectFirst("a[href*=drama]")?.attr("href")?.substringAfterLast("/").orEmpty(),
                title = el.selectFirst(".film-title, h2, h3")?.text().orEmpty(),
                posterUrl = el.selectFirst("img")?.attr("src").orEmpty(),
                rating = el.selectFirst(".rating")?.text().orEmpty(),
                isPremium = el.selectFirst(".premium") != null,
            )
        }.filter { it.slug.isNotEmpty() }
    }

    suspend fun fetchOnGoing(): List<Drama> = io {
        val doc = Jsoup.parse(get("https://drakorid.co/ongoing"))
        doc.select(".movie-list .poster, .movie-list .content").map { el ->
            Drama(
                id = (el.selectFirst("[data-id]")?.attr("data-id")?.toIntOrNull() ?: 0),
                slug = el.selectFirst("a[href*=drama]")?.attr("href")?.substringAfterLast("/").orEmpty(),
                title = el.selectFirst(".film-title, h2, h3")?.text().orEmpty(),
                posterUrl = el.selectFirst("img")?.attr("src").orEmpty(),
                rating = el.selectFirst(".rating")?.text().orEmpty(),
                isPremium = el.selectFirst(".premium") != null,
            )
        }.filter { it.slug.isNotEmpty() }
    }

    suspend fun fetchComplete(): List<Drama> = io {
        val doc = Jsoup.parse(get("https://drakorid.co/complete"))
        doc.select(".movie-list .poster, .movie-list .content").map { el ->
            Drama(
                id = (el.selectFirst("[data-id]")?.attr("data-id")?.toIntOrNull() ?: 0),
                slug = el.selectFirst("a[href*=drama]")?.attr("href")?.substringAfterLast("/").orEmpty(),
                title = el.selectFirst(".film-title, h2, h3")?.text().orEmpty(),
                posterUrl = el.selectFirst("img")?.attr("src").orEmpty(),
                rating = el.selectFirst(".rating")?.text().orEmpty(),
                isPremium = el.selectFirst(".premium") != null,
            )
        }.filter { it.slug.isNotEmpty() }
    }

    suspend fun fetchMovie(): List<Drama> = io {
        val doc = Jsoup.parse(get("https://drakorid.co/movie"))
        doc.select(".movie-list .poster, .movie-list .content").map { el ->
            Drama(
                id = (el.selectFirst("[data-id]")?.attr("data-id")?.toIntOrNull() ?: 0),
                slug = el.selectFirst("a[href*=drama]")?.attr("href")?.substringAfterLast("/").orEmpty(),
                title = el.selectFirst(".film-title, h2, h3")?.text().orEmpty(),
                posterUrl = el.selectFirst("img")?.attr("src").orEmpty(),
                rating = el.selectFirst(".rating")?.text().orEmpty(),
                isPremium = el.selectFirst(".premium") != null,
            )
        }.filter { it.slug.isNotEmpty() }
    }

    // ------------------------------------------------------------------ //
    //  Drama detail                                                       //
    // ------------------------------------------------------------------ //

    suspend fun fetchDramaDetail(slug: String): Pair<Drama, List<EpisodeEntry>> = io {
        val html = get("https://drakorid.co/drama/$slug")
        val doc = Jsoup.parse(html)
        val drama = Drama(
            id = (doc.selectFirst("[data-id]")?.attr("data-id")?.toIntOrNull() ?: 0),
            slug = slug,
            title = doc.selectFirst(".film-title, h1")?.text().orEmpty(),
            posterUrl = doc.selectFirst("img")?.attr("src").orEmpty(),
            rating = doc.selectFirst(".rating, .score")?.text().orEmpty(),
            year = doc.selectFirst(".year")?.text().orEmpty(),
            episodes = doc.selectFirst(".episodes, .ep-count")?.text().orEmpty(),
            status = doc.selectFirst(".status")?.text().orEmpty(),
            isPremium = doc.selectFirst(".premium, .crown-icon") != null,
        )
        val episodes = doc.select(".episode-list a, .episode-item a").mapIndexedNotNull { idx, el ->
            val href = el.attr("href")
            if (href.isBlank()) null
            else EpisodeEntry(
                id = idx,
                number = idx + 1,
                slug = href.substringAfterLast("/"),
                title = el.text().ifBlank { "Ep ${idx + 1}" },
                url = href,
            )
        }
        drama to episodes
    }

    // ------------------------------------------------------------------ //
    //  Episode detail (JSON API)                                           //
    // ------------------------------------------------------------------ //

    suspend fun resolveEpisode(
        slug: String,
        episodeNumber: Int,
    ): EpisodeDetail? = io {
        val token = tokenProvider.get()
        val ajaxUrl = "https://drakorid.co/myapi/episode_detail.php?slug=$slug&episode=$episodeNumber&token=$token"
        val json = get(ajaxUrl, xhr = true)
        val fileId = Regex(""""file_id"\s*:\s*"?(\d+)"?""").find(json)?.groupValues?.get(1) ?: return@io null
        val direct = "https://admin.drakor.la/go/files/$fileId"
        EpisodeDetail(
            dramaId = 0,
            title = slug,
            episode = episodeNumber,
            fileId = fileId,
            directUrl = direct,
            isPremium = false,
            qualities = listOf(QualityOption("default", direct, "")),
        )
    }

    /**
     * Resolves 360p / 480p / 720p from the /watch-fast page.
     * The page embeds 3 iframes whose srcdoc is base64-wrapped.
     * Parses out the actual mp4 URL from each base64 body.
     */
    suspend fun resolveQualities(
        slug: String,
        episodeSlug: String,
    ): List<QualityOption> = io {
        val html = get("https://drakorid.co/watch-fast/$slug/$episodeSlug")
        val doc = Jsoup.parse(html)
        val iframes = doc.select("iframe[srcdoc]")
        iframes.mapIndexedNotNull { idx, iframe ->
            val srcdoc = iframe.attr("srcdoc")
            val decoded = try { android.util.Base64.decode(srcdoc, android.util.Base64.DEFAULT).toString(Charsets.UTF_8) } catch (_: Exception) { srcdoc }
            val mp4 = Regex("""https?://[^"'\s]+\.mp4[^"'\s]*""").find(decoded)?.value ?: return@mapIndexedNotNull null
            val label = when (idx) {
                0 -> "360p"
                1 -> "480p"
                else -> "720p"
            }
            QualityOption(label, mp4, srcdoc)
        }
    }

    // ------------------------------------------------------------------ //
    //  Search                                                              //
    // ------------------------------------------------------------------ //

    suspend fun search(query: String): List<Drama> = io {
        if (query.isBlank()) return@io emptyList()
        val doc = Jsoup.parse(get("https://drakorid.co/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}"))
        doc.select(".movie-list .poster, .movie-list .content").map { el ->
            Drama(
                id = (el.selectFirst("[data-id]")?.attr("data-id")?.toIntOrNull() ?: 0),
                slug = el.selectFirst("a[href*=drama]")?.attr("href")?.substringAfterLast("/").orEmpty(),
                title = el.selectFirst(".film-title, h2, h3")?.text().orEmpty(),
                posterUrl = el.selectFirst("img")?.attr("src").orEmpty(),
                rating = el.selectFirst(".rating")?.text().orEmpty(),
                isPremium = el.selectFirst(".premium") != null,
            )
        }.filter { it.slug.isNotEmpty() }
    }

    // ------------------------------------------------------------------ //
    //  Category / alphabet                                                 //
    // ------------------------------------------------------------------ //

    suspend fun fetchCategories(): List<Category> = io {
        val html = get("https://drakorid.co/category")
        val doc = Jsoup.parse(html)
        doc.select("a[href*=category]").mapNotNull { el ->
            val href = el.attr("href")
            val id = Regex("""id=(\d+)""").find(href)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val count = Regex("""\((\d+)\)""").find(el.text())?.groupValues?.get(1)?.toIntOrNull() ?: 0
            Category(
                name = el.text().replace(Regex("""\s*\(\d+\)"""), "").trim(),
                id = id,
                slug = href.substringAfterLast("/").substringBefore("?"),
                count = count,
            )
        }.filter { it.name.isNotBlank() }
    }

    suspend fun fetchCategoryPage(slug: String, page: Int = 1): List<Drama> = io {
        val url = "https://drakorid.co/category/$slug${if (page > 1) "?page=$page" else ""}"
        val doc = Jsoup.parse(get(url))
        doc.select(".movie-list .poster, .movie-list .content").map { el ->
            Drama(
                id = (el.selectFirst("[data-id]")?.attr("data-id")?.toIntOrNull() ?: 0),
                slug = el.selectFirst("a[href*=drama]")?.attr("href")?.substringAfterLast("/").orEmpty(),
                title = el.selectFirst(".film-title, h2, h3")?.text().orEmpty(),
                posterUrl = el.selectFirst("img")?.attr("src").orEmpty(),
                rating = el.selectFirst(".rating")?.text().orEmpty(),
                isPremium = el.selectFirst(".premium") != null,
            )
        }.filter { it.slug.isNotEmpty() }
    }

    suspend fun fetchAlphabet(): List<Pair<String, List<Drama>>> = io {
        val doc = Jsoup.parse(get("https://drakorid.co/alphabet"))
        val results = mutableListOf<Pair<String, List<Drama>>>()
        doc.select("h2, .alphabet-header, .letter").forEach { header ->
            val letter = header.text().take(1).uppercase()
            val dramas = mutableListOf<Drama>()
            var sibling = header.nextElementSibling()
            while (sibling != null && sibling.tagName() != "h2" && !sibling.`is`(".alphabet-header")) {
                sibling.select(".poster, .content").forEach { el ->
                    val slug = el.selectFirst("a[href*=drama]")?.attr("href")?.substringAfterLast("/").orEmpty()
                    if (slug.isNotEmpty()) {
                        dramas += Drama(
                            id = 0,
                            slug = slug,
                            title = el.selectFirst(".film-title, h2, h3")?.text().orEmpty(),
                            posterUrl = el.selectFirst("img")?.attr("src").orEmpty(),
                            rating = el.selectFirst(".rating")?.text().orEmpty(),
                            isPremium = el.selectFirst(".premium") != null,
                        )
                    }
                }
                sibling = sibling.nextElementSibling()
            }
            if (letter.isNotEmpty() && dramas.isNotEmpty()) results += letter to dramas
        }
        results
    }

    // ------------------------------------------------------------------ //
    //  HTTP helpers                                                        //
    // ------------------------------------------------------------------ //

    private fun get(url: String, xhr: Boolean = false): String {
        val builder = Request.Builder().url(url).get()
        if (xhr) {
            builder.header("X-Requested-With", "XMLHttpRequest")
            builder.header("Referer", "https://drakorid.co/")
        }
        builder.header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
        return client.newCall(builder.build()).execute().use { it.body?.string().orEmpty() }
    }

    private suspend fun <T> io(block: suspend () -> T): T = withContext(Dispatchers.IO) { block() }
}