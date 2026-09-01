package com.drakorid.stream.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.drakorid.stream.data.remote.DramaRepository
import com.drakorid.stream.domain.model.Drama
import com.drakorid.stream.domain.model.EpisodeEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DramaDetailScreen(
    repository: DramaRepository,
    slug: String,
    onEpisodeClick: (slug: String, episodeNumber: Int) -> Unit,
) {
    var drama by remember { mutableStateOf<Drama?>(null) }
    var episodes by remember { mutableStateOf<List<EpisodeEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(slug) {
        val result = repository.fetchDramaDetail(slug)
        drama = result.first
        episodes = result.second
        loading = false
    }

    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        drama == null -> Text("Drama not found.", Modifier.padding(16.dp))
        else -> LazyColumn(Modifier.fillMaxSize().statusBarsPadding()) {
            item {
                Column(Modifier.padding(16.dp)) {
                    Row {
                        AsyncImage(
                            model = drama!!.posterUrl,
                            contentDescription = drama!!.title,
                            modifier = Modifier.width(120.dp).aspectRatio(2f / 3f),
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(drama!!.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            if (drama!!.rating.isNotBlank()) {
                                Text("Rating: ${drama!!.rating}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (drama!!.year.isNotBlank()) {
                                Text("Year: ${drama!!.year}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (drama!!.status.isNotBlank()) {
                                Text("Status: ${drama!!.status}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Episodes (${episodes.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                }
            }
            items(episodes) { ep ->
                ListItem(
                    headlineContent = { Text(ep.title) },
                    supportingContent = { Text("Episode ${ep.number}") },
                    modifier = Modifier.clickable { onEpisodeClick(slug, ep.number) },
                    leadingContent = {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${ep.number}", style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    },
                )
                HorizontalDivider()
            }
        }
    }
}