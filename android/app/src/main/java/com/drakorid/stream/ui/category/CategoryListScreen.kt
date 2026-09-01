package com.drakorid.stream.ui.category

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
import com.drakorid.stream.domain.model.Category
import com.drakorid.stream.domain.model.Drama

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    repository: DramaRepository,
    onCategoryClick: (String, String) -> Unit,
) {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        categories = repository.fetchCategories()
        loading = false
    }

    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories) { cat ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onCategoryClick(cat.slug, cat.name) },
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(cat.name, style = MaterialTheme.typography.titleSmall)
                        if (cat.count > 0) {
                            Text("${cat.count} titles", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    repository: DramaRepository,
    slug: String,
    name: String,
    onDramaClick: (String) -> Unit,
) {
    var dramas by remember { mutableStateOf<List<Drama>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(slug) {
        dramas = repository.fetchCategoryPage(slug)
        loading = false
    }

    Scaffold(topBar = { TopAppBar(title = { Text(name) }) }) { pad ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(pad),
            ) {
                items(dramas) { drama ->
                    Column(
                        modifier = Modifier.clickable { onDramaClick(drama.slug) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AsyncImage(
                            model = drama.posterUrl,
                            contentDescription = drama.title,
                            modifier = Modifier.aspectRatio(2f / 3f).fillMaxWidth(),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(drama.title, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                    }
                }
            }
        }
    }
}