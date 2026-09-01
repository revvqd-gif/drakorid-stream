package com.drakorid.stream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.drakorid.stream.data.download.DownloadScheduler
import com.drakorid.stream.data.local.HistoryRepository
import com.drakorid.stream.data.remote.DramaRepository
import com.drakorid.stream.ui.approot.AppRoot
import com.drakorid.stream.ui.theme.DrakoridStreamTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var repository: DramaRepository
    @Inject lateinit var historyRepo: HistoryRepository
    @Inject lateinit var downloadScheduler: DownloadScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrakoridStreamTheme {
                AppRoot(
                    repository = repository,
                    historyRepo = historyRepo,
                    downloadScheduler = downloadScheduler,
                )
            }
        }
    }
}