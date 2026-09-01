package com.drakorid.stream.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.content.Context

@Entity(tableName = "watch_history")
data class HistoryEntity(
    @PrimaryKey @ColumnInfo(name = "drama_id") val dramaId: Int,
    @ColumnInfo(name = "slug") val slug: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "poster_url") val posterUrl: String,
    @ColumnInfo(name = "episode") val episode: Int,
    @ColumnInfo(name = "position_ms") val positionMs: Long,
    @ColumnInfo(name = "duration_ms") val durationMs: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Entity(tableName = "download")
data class DownloadEntity(
    @PrimaryKey @ColumnInfo(name = "file_id") val fileId: String,
    @ColumnInfo(name = "drama_id") val dramaId: Int,
    @ColumnInfo(name = "slug") val slug: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "poster_url") val posterUrl: String,
    @ColumnInfo(name = "episode") val episode: Int,
    @ColumnInfo(name = "quality") val quality: String,
    @ColumnInfo(name = "direct_url") val directUrl: String,
    @ColumnInfo(name = "state") val state: String,
    @ColumnInfo(name = "progress") val progress: Int = 0,
    @ColumnInfo(name = "bytes_downloaded") val bytesDownloaded: Long = 0,
    @ColumnInfo(name = "total_bytes") val totalBytes: Long = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HistoryEntity)

    @Query("SELECT * FROM watch_history ORDER BY updated_at DESC LIMIT :limit")
    suspend fun recent(limit: Int = 50): List<HistoryEntity>

    @Query("SELECT * FROM watch_history WHERE drama_id = :dramaId LIMIT 1")
    suspend fun get(dramaId: Int): HistoryEntity?

    @Query("DELETE FROM watch_history WHERE drama_id = :dramaId")
    suspend fun delete(dramaId: Int)

    @Query("DELETE FROM watch_history")
    suspend fun clear()
}

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadEntity)

    @Query("SELECT * FROM download ORDER BY created_at DESC")
    suspend fun all(): List<DownloadEntity>

    @Query("SELECT * FROM download WHERE file_id = :fileId LIMIT 1")
    suspend fun get(fileId: String): DownloadEntity?

    @Query("UPDATE download SET state = :state, progress = :progress, bytes_downloaded = :bytes, total_bytes = :total WHERE file_id = :fileId")
    suspend fun updateProgress(fileId: String, state: String, progress: Int, bytes: Long, total: Long)

    @Query("DELETE FROM download WHERE file_id = :fileId")
    suspend fun delete(fileId: String)

    @Query("DELETE FROM download")
    suspend fun clear()
}

@Database(
    entities = [HistoryEntity::class, DownloadEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "drakorid.db",
            ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}