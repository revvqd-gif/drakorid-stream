package com.drakorid.stream.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.drakorid.stream.data.local.HistoryRepository
import com.drakorid.stream.domain.model.HistoryEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyRepo: HistoryRepository,
    onDramaClick: (String) -> Unit,
) {
    val entries by historyRepo.flow.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { historyRepo.refresh() }

    when {
        entries.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No watch history yet.", style = MaterialTheme.typography.bodyMedium)
        }
        else -> LazyColumn(Modifier.fillMaxSize().statusBarsPadding()) {
            items(entries) { entry ->
                ListItem(
                    headlineContent = { Text(entry.title) },
                    supportingContent = {
                        Text("Ep ${entry.episode} · ${entry.durationMs / 1000}s")
                    },
                    modifier = Modifier.clickable { onDramaClick(entry.slug) },
                    leadingContent = {
                        AsyncImage(
                            model = entry.posterUrl,
                            contentDescription = entry.title,
                            modifier = Modifier.size(48.dp),
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = { scope.launch { historyRepo.delete(entry.dramaId) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    },
                )
                HorizontalDivider()
            }
        }
    }
}