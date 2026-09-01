package com.drakorid.stream.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.drakorid.stream.data.remote.DramaRepository
import com.drakorid.stream.domain.model.Drama

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    repository: DramaRepository,
    onDramaClick: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Drama>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        if (query.length >= 2) {
            loading = true
            results = repository.search(query)
            loading = false
        } else {
            results = emptyList()
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search drakor...") },
            singleLine = true,
        )
        Spacer(Modifier.height(16.dp))
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            results.isEmpty() && query.length >= 2 -> Text("No results found.", style = MaterialTheme.typography.bodyMedium)
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(results) { drama ->
                    DramaCard(drama, onDramaClick)
                }
            }
        }
    }
}

@Composable
private fun DramaCard(drama: Drama, onClick: (String) -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick(drama.slug) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = drama.posterUrl,
            contentDescription = drama.title,
            modifier = Modifier.aspectRatio(2f / 3f).fillMaxWidth(),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = drama.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
        )
    }
}