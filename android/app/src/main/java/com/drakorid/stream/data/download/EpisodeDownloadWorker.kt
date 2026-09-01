package com.drakorid.stream.data.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.drakorid.stream.data.local.DownloadEntity
import com.drakorid.stream.data.local.DownloadDao
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

class EpisodeDownloadWorker(
    appContext: Context,
    params: WorkerParameters,
) : Worker(appContext, params) {

    override fun doWork(): Result {
        val fileId = inputData.getString(KEY_FILE_ID) ?: return Result.failure()
        val directUrl = inputData.getString(KEY_URL) ?: return Result.failure()
        val dramaId = inputData.getInt(KEY_DRAMA_ID, 0)
        val slug = inputData.getString(KEY_SLUG).orEmpty()
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val posterUrl = inputData.getString(KEY_POSTER).orEmpty()
        val episode = inputData.getInt(KEY_EPISODE, 0)
        val quality = inputData.getString(KEY_QUALITY).orEmpty()

        val targetDir = File(
            applicationContext.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES),
            "DrakoridStream",
        ).apply { mkdirs() }
        val targetFile = File(targetDir, "$fileId.mp4")

        val client = OkHttpClient.Builder().build()
        val request = Request.Builder().url(directUrl).build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return Result.failure()
                val body = response.body ?: return Result.failure()
                val total = body.contentLength()
                val out = targetFile.outputStream()
                val src = body.byteStream()
                var downloaded = 0L
                val buf = ByteArray(64 * 1024)
                while (true) {
                    val n = src.read(buf)
                    if (n == -1) break
                    out.write(buf, 0, n)
                    downloaded += n
                }
                out.flush(); out.close()
            }
            Result.success()
        } catch (e: Throwable) {
            Timber.e(e, "download failed: $fileId")
            if (targetFile.exists()) targetFile.delete()
            Result.failure()
        }
    }

    companion object {
        const val KEY_FILE_ID = "fileId"
        const val KEY_URL = "url"
        const val KEY_DRAMA_ID = "dramaId"
        const val KEY_SLUG = "slug"
        const val KEY_TITLE = "title"
        const val KEY_POSTER = "posterUrl"
        const val KEY_EPISODE = "episode"
        const val KEY_QUALITY = "quality"
        const val WORK_NAME_PREFIX = "download-"
    }
}

@Singleton
class DownloadScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: OkHttpClient,
    private val dao: DownloadDao,
) {
    fun enqueue(
        fileId: String, directUrl: String,
        dramaId: Int, slug: String, title: String, posterUrl: String,
        episode: Int, quality: String,
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data = Data.Builder()
            .putString(EpisodeDownloadWorker.KEY_FILE_ID, fileId)
            .putString(EpisodeDownloadWorker.KEY_URL, directUrl)
            .putInt(EpisodeDownloadWorker.KEY_DRAMA_ID, dramaId)
            .putString(EpisodeDownloadWorker.KEY_SLUG, slug)
            .putString(EpisodeDownloadWorker.KEY_TITLE, title)
            .putString(EpisodeDownloadWorker.KEY_POSTER, posterUrl)
            .putInt(EpisodeDownloadWorker.KEY_EPISODE, episode)
            .putString(EpisodeDownloadWorker.KEY_QUALITY, quality)
            .build()

        val req = OneTimeWorkRequestBuilder<EpisodeDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                EpisodeDownloadWorker.WORK_NAME_PREFIX + fileId,
                ExistingWorkPolicy.KEEP,
                req,
            )
    }

    suspend fun loadAll(): List<DownloadEntity> = dao.all()
    suspend fun delete(fileId: String) = dao.delete(fileId)
}