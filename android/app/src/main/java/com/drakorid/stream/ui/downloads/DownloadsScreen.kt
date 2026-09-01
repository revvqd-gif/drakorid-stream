package com.drakorid.stream.ui.downloads

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.drakorid.stream.data.download.DownloadScheduler
import com.drakorid.stream.domain.model.DownloadState
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    downloadScheduler: DownloadScheduler,
) {
    var items by remember { mutableStateOf<List<DownloadState>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        items = downloadScheduler.loadAll().map {
            DownloadState(
                fileId = it.fileId, dramaId = it.dramaId, slug = it.slug,
                title = it.title, posterUrl = it.posterUrl, episode = it.episode,
                quality = it.quality, directUrl = it.directUrl, state = it.state,
                progress = it.progress, bytesDownloaded = it.bytesDownloaded,
                totalBytes = it.totalBytes, createdAt = it.createdAt,
            )
        }
    }

    when {
        items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No downloads yet.", style = MaterialTheme.typography.bodyMedium)
        }
        else -> LazyColumn(Modifier.fillMaxSize().statusBarsPadding(), contentPadding = PaddingValues(16.dp)) {
            items(items) { item ->
                val file = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES), "DrakoridStream/${item.fileId}.mp4")
                ListItem(
                    headlineContent = { Text(item.title) },
                    supportingContent = {
                        Column {
                            Text("Ep ${item.episode} · ${item.quality}")
                            if (item.state == "DOWNLOADING") {
                                LinearProgressIndicator(
                                    progress = { item.progress / 100f },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                )
                                Text("${item.progress}%", style = MaterialTheme.typography.bodySmall)
                            } else if (item.state == "COMPLETED") {
                                Text("Completed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            } else if (item.state == "FAILED") {
                                Text("Failed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    leadingContent = {
                        AsyncImage(model = item.posterUrl, contentDescription = null, modifier = Modifier.size(48.dp))
                    },
                    trailingContent = {
                        Row {
                            if (item.state == "COMPLETED" && file.exists()) {
                                IconButton(onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.fromFile(file), "video/mp4")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                                }
                            }
                            IconButton(onClick = {
                                scope.launch { downloadScheduler.delete(item.fileId) }
                                if (file.exists()) file.delete()
                                items = items.filter { it.fileId != item.fileId }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    },
                )
                HorizontalDivider()
            }
        }
    }
}