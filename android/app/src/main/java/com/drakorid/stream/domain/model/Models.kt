package com.drakorid.stream.domain.model

data class Category(
    val name: String,
    val id: Int,
    val slug: String,
    val count: Int,
)

data class Drama(
    val id: Int,
    val slug: String,
    val title: String,
    val posterUrl: String,
    val rating: String = "",
    val year: String = "",
    val episodes: String = "",
    val status: String = "",
    val isPremium: Boolean = false,
)

data class EpisodeEntry(
    val id: Int,
    val number: Int,
    val slug: String,
    val title: String,
    val url: String,
)

data class HistoryEntry(
    val dramaId: Int,
    val slug: String,
    val title: String,
    val posterUrl: String,
    val episode: Int,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,
)

data class DownloadState(
    val fileId: String,
    val dramaId: Int,
    val slug: String,
    val title: String,
    val posterUrl: String,
    val episode: Int,
    val quality: String,
    val directUrl: String,
    val state: String,
    val progress: Int,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val createdAt: Long,
)

data class QualityOption(
    val label: String,
    val directUrl: String,
    val base64Body: String,
)

data class EpisodeDetail(
    val dramaId: Int,
    val title: String,
    val episode: Int,
    val fileId: String,
    val directUrl: String,
    val isPremium: Boolean,
    val qualities: List<QualityOption>,
)