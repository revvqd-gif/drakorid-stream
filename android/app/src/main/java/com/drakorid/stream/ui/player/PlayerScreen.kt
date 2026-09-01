package com.drakorid.stream.ui.player

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.drakorid.stream.data.local.HistoryRepository
import com.drakorid.stream.data.remote.DramaRepository
import com.drakorid.stream.domain.model.HistoryEntry
import kotlinx.coroutines.launch
import timber.log.Timber

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PlayerScreen(
    repository: DramaRepository,
    historyRepo: HistoryRepository,
    dramaId: Int,
    slug: String,
    episodeNumber: Int,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current as ComponentActivity
    val scope = rememberCoroutineScope()

    var directUrl by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(slug, episodeNumber) {
        loading = true; error = null
        try {
            val detail = repository.resolveEpisode(slug, episodeNumber)
            if (detail != null) directUrl = detail.directUrl
            else error = "Could not resolve episode."
        } catch (e: Throwable) {
            error = e.message ?: "Unknown error"
        }
        loading = false
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer, directUrl) {
        directUrl?.let { url ->
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
            exoPlayer.prepare()
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        scope.launch {
                            historyRepo.save(
                                HistoryEntry(
                                    dramaId = dramaId,
                                    slug = slug,
                                    title = slug,
                                    posterUrl = "",
                                    episode = episodeNumber,
                                    positionMs = exoPlayer.currentPosition,
                                    durationMs = exoPlayer.duration,
                                    updatedAt = System.currentTimeMillis(),
                                )
                            )
                        }
                    }
                }
            })
        }
        onDispose { exoPlayer.release() }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            error != null -> {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onBack) { Text("Go back") }
                }
            }
            else -> PlayerView(player = exoPlayer, modifier = Modifier.fillMaxSize())
        }
    }
}