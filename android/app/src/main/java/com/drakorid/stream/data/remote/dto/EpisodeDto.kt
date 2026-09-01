package com.drakorid.stream.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `episode_detail.php` response.
 * Status = 1 → success, else error_msg populated.
 */
@Serializable
data class EpisodeDetailDto(
    val status: Int = 0,
    val tipe: String? = null,
    val sid: String? = null,
    @SerialName("streaming_premium") val streamingPremium: String? = null,
    val firebase: String? = null,
    @SerialName("streaming_amazon") val streamingAmazon: String? = null,
    @SerialName("episode_name") val episodeName: String? = null,
    @SerialName("link_alternatif") val linkAlternatif: String? = null,
    val streaming: String? = null,
    val img: String? = null,
    @SerialName("streaming_firebase") val streamingFirebase: String? = null,
    @SerialName("is_any_cdn") val isAnyCdn: Boolean = false,
    @SerialName("is_any_rtmp") val isAnyRtmp: Boolean = false,
    @SerialName("is_any_alternative") val isAnyAlternative: Boolean = false,
    @SerialName("error_msg") val errorMsg: String? = null,
)

/** `jumlah_komentar.php` response. */
@Serializable
data class JumlahKomentarDto(
    val status: Int = 0,
    @SerialName("jumlah_komentar") val jumlahKomentar: Int = 0,
    @SerialName("error_msg") val errorMsg: String? = null,
)

/** Server-side full-text search response (raw HTML). */
typealias SearchHtml = String

/** Watch page response (used to extract iframe-quality URLs). */
typealias WatchPageHtml = String

/** Result of resolving a `streaming` file ID to its direct mp4 URL. */
@Serializable
data class ResolvedStream(
    val fileId: String,
    val directUrl: String,
    val qualities: Map<String, String>, // quality label → url
)