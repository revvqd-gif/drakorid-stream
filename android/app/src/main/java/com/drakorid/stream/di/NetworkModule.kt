package com.drakorid.stream.di

import android.content.Context
import com.drakorid.stream.data.local.AppDatabase
import com.drakorid.stream.data.local.DownloadDao
import com.drakorid.stream.data.local.HistoryDao
import com.drakorid.stream.data.local.HistoryRepository
import com.drakorid.stream.data.download.DownloadScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase = AppDatabase.get(context)

    @Provides fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao()
    @Provides fun provideDownloadDao(db: AppDatabase): DownloadDao = db.downloadDao()

    @Provides
    @Singleton
    fun provideHistoryRepository(dao: HistoryDao): HistoryRepository = HistoryRepository(dao)

    @Provides
    @Singleton
    fun provideDownloadScheduler(
        @ApplicationContext context: Context,
        client: OkHttpClient,
        dao: DownloadDao,
    ): DownloadScheduler = DownloadScheduler(context, client, dao)
}