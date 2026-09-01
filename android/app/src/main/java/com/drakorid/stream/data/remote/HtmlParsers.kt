package com.drakorid.stream.data.remote

import com.drakorid.stream.domain.model.Drama
import com.drakorid.stream.domain.model.Episode

/**
 * Lightweight regex parser for the HTML fragments returned by drakorid.co AJAX
 * endpoints. The backend renders Bootstrap card markup like:
 *
 *   <article class="movie-list-card">
 *     <a href="https://drakorid.co/go/5025" class="movie-list-card__media">
 *       <img src="https://convert.d-cdn.me/convert/.../180x200/1.jpg" alt="My Bias My Boss">
 *     </a>
 *   </article>
 *
 * `/go/{id}` is a 302 redirect to `/nonton/{slug}/`. We extract the ID and use
 * `/nonton/{slug}/` later to learn the slug — for card lists we use the title
 * from the `alt` attribute and resolve to the canonical URL on click.
 */
object DramaListParser {

    private val CARD_REGEX = Regex(
        """<article[^>]*movie-list-card[^>]*>\s*<a[^>]+href="([^"]+)"[^>]*>\s*<img[^>]+src="([^"]+)"[^>]*(?:alt="([^"]*)")?""",
        RegexOption.DOT_MATCHES_ALL,
    )

    private val SLIDER_REGEX = Regex(
        """<div[^>]*owl-item[^>]*>\s*<a[^>]+href="([^"]+)"[^>]*>\s*<img[^>]+src="([^"]+)"[^>]*(?:alt="([^"]*)")?""",
        RegexOption.DOT_MATCHES_ALL,
    )

    fun parseCards(html: String): List<Drama> {
        return CARD_REGEX.findAll(html).mapNotNull { m ->
            val href = m.groupValues[1]
            val img = m.groupValues[2]
            val title = m.groupValues[3]
            parse(href, img, title)
        }.toList()
    }

    fun parseSlider(html: String): List<Drama> {
        return SLIDER_REGEX.findAll(html).mapNotNull { m ->
            val href = m.groupValues[1]
            val img = m.groupValues[2]
            val title = m.groupValues[3]
            parse(href, img, title)
        }.toList()
    }

    private fun parse(href: String, img: String, title: String): Drama? {
        // href like https://drakorid.co/go/5025
        val id = href.substringAfterLast('/').toIntOrNull() ?: return null
        return Drama(
            id = id,
            slug = "", // resolved on detail screen
            title = title.ifBlank { "Drama #$id" },
            posterUrl = img,
        )
    }
}

object EpisodeListParser {

    private val EPISODE_BUTTON_REGEX = Regex(
        """data-episode="(\d+)"[^>]*data-ep-label="([^"]*)"""",
    )

    private val IS_PREMIUM_REGEX = Regex("""is-premium""")

    fun parse(html: String): List<Episode> {
        val isPremiumHtml = html.contains("is-premium")
        return EPISODE_BUTTON_REGEX.findAll(html).map { m ->
            val ep = m.groupValues[1].toInt()
            val label = m.groupValues[2].ifBlank { "Episode $ep" }
            Episode(
                dramaId = 0,
                episodeNumber = ep,
                label = label,
                isPremium = isPremiumHtml,
            )
        }.toList()
    }
}