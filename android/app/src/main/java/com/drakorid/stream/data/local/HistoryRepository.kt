package com.drakorid.stream.data.local

import com.drakorid.stream.domain.model.HistoryEntry
import com.drakorid.stream.domain.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local-only watch history. Persisted in Room; observed via a StateFlow
 * that re-emits on every write so the History screen updates live.
 */
@Singleton
class HistoryRepository @Inject constructor(
    private val dao: HistoryDao,
) {
    private val _flow = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val flow: kotlinx.coroutines.flow.StateFlow<List<HistoryEntry>> = _flow.asStateFlow()

    suspend fun refresh() {
        _flow.value = dao.recent().map { it.toDomain() }
    }

    suspend fun save(entry: HistoryEntry): Result<Unit> = runCatching {
        dao.upsert(entry.toEntity())
        refresh()
    }

    suspend fun get(dramaId: Int): HistoryEntry? = dao.get(dramaId)?.toDomain()

    suspend fun delete(dramaId: Int): Result<Unit> = runCatching {
        dao.delete(dramaId)
        refresh()
    }

    suspend fun clear(): Result<Unit> = runCatching {
        dao.clear()
        refresh()
    }
}

private fun HistoryEntity.toDomain() = HistoryEntry(
    dramaId = dramaId,
    slug = slug,
    title = title,
    posterUrl = posterUrl,
    episode = episode,
    positionMs = positionMs,
    durationMs = durationMs,
    updatedAt = updatedAt,
)

private fun HistoryEntry.toEntity() = HistoryEntity(
    dramaId = dramaId,
    slug = slug,
    title = title,
    posterUrl = posterUrl,
    episode = episode,
    positionMs = positionMs,
    durationMs = durationMs,
    updatedAt = updatedAt,
)