package com.drakorid.stream.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.drakorid.stream.data.remote.DramaRepository
import com.drakorid.stream.domain.model.Drama

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: DramaRepository,
    onDramaClick: (String) -> Unit,
) {
    var popular by remember { mutableStateOf<List<Drama>>(emptyList()) }
    var latest by remember { mutableStateOf<List<Drama>>(emptyList()) }
    var ongoing by remember { mutableStateOf<List<Drama>>(emptyList()) }
    var complete by remember { mutableStateOf<List<Drama>>(emptyList()) }
    var movie by remember { mutableStateOf<List<Drama>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            coroutineScope {
                val p = async { repository.fetchPopular() }
                val l = async { repository.fetchLatest() }
                val o = async { repository.fetchOnGoing() }
                val c = async { repository.fetchComplete() }
                val m = async { repository.fetchMovie() }
                popular = p.await(); latest = l.await(); ongoing = o.await(); complete = c.await(); movie = m.await()
            }
        } catch (e: Throwable) {
            error = e.message
        }
        loading = false
    }

    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(error!!, color = MaterialTheme.colorScheme.error) }
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item { SectionHeader("Popular") }
            item { DramaRow(popular, onDramaClick) }
            item { SectionHeader("Latest Updates") }
            item { DramaRow(latest, onDramaClick) }
            item { SectionHeader("Ongoing") }
            item { DramaRow(ongoing, onDramaClick) }
            item { SectionHeader("Complete") }
            item { DramaRow(complete, onDramaClick) }
            if (movie.isNotEmpty()) {
                item { SectionHeader("Movies") }
                item { DramaRow(movie, onDramaClick) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun DramaRow(dramas: List<Drama>, onClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(dramas) { drama ->
            DramaCard(drama, onClick)
        }
    }
}

@Composable
private fun DramaCard(drama: Drama, onClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick(drama.slug) }
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            AsyncImage(
                model = drama.posterUrl,
                contentDescription = drama.title,
                modifier = Modifier
                    .aspectRatio(2f / 3f)
                    .fillMaxWidth(),
            )
            if (drama.isPremium) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                ) {
                    Text(
                        "PREMIUM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = drama.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (drama.rating.isNotBlank()) {
            Text(
                drama.rating,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}