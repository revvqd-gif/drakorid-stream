package com.drakorid.stream.ui.navigation

import kotlinx.serialization.Serializable

@Serializable object Home
@Serializable object Search
@Serializable object Schedule
@Serializable object History
@Serializable object Downloads

@Serializable data class CategoryList(val slug: String, val title: String)
@Serializable data class ListAlphabet(val page: Int)
@Serializable data class DramaDetail(val slug: String, val dramaId: Int, val title: String)
@Serializable data class ArtistDetail(val id: Int, val name: String)
@Serializable data class OSTDetail(val id: Int, val title: String)
@Serializable data class Player(
    val dramaId: Int,
    val slug: String,
    val episode: Int,
    val title: String,
    val posterUrl: String? = null,
    val startPositionMs: Long = 0L,
)