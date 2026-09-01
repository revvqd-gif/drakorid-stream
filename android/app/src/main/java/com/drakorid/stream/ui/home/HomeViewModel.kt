package com.drakorid.stream.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drakorid.stream.data.remote.DramaRepository
import com.drakorid.stream.domain.model.Drama
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val sections: Map<String, List<Drama>> = emptyMap(),
    val error: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && error == null && sections.isEmpty()
    val hasContent: Boolean get() = sections.values.any { it.isNotEmpty() }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: DramaRepository,
    savedState: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState(isLoading = true))
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val results = mapOf(
                "slider" to repo.fetchSlider(),
                "trending" to repo.fetchTrending(),
                "ongoing" to repo.fetchOngoing(),
                "newest" to repo.fetchNewest(),
                "favorite" to repo.fetchFavorite(),
                "drama_korea" to repo.fetchCategoryNewest("Drama Korea"),
                "drama_china" to repo.fetchCategoryNewest("Drama China"),
            )

            val sections = LinkedHashMap<String, List<Drama>>()
            var firstError: Throwable? = null
            for ((key, result) in results) {
                result.fold(
                    onSuccess = { list -> if (list.isNotEmpty()) sections[key] = list },
                    onFailure = { e -> if (firstError == null) firstError = e },
                )
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    sections = sections,
                    error = firstError?.message,
                )
            }
        }
    }
}