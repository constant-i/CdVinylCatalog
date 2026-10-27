package com.kai.cdvinylcatalog.feature.add.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Введённый запрос переживает смерть процесса: экран поиска — это форма,
    // и терять введённое без причины незачем
    private val _state = MutableStateFlow(
        SearchContract.State(query = savedStateHandle[KEY_QUERY] ?: "")
    )
    val state = _state.asStateFlow()

    private val _effect = Channel<SearchContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: SearchContract.Intent) {
        when (intent) {
            is SearchContract.Intent.OnQueryChanged -> {
                savedStateHandle[KEY_QUERY] = intent.query
                _state.update { it.copy(query = intent.query) }
            }
            SearchContract.Intent.OnSearchClicked -> {
                val query = _state.value.query.trim()
                if (query.isNotBlank()) {
                    viewModelScope.launch {
                        _effect.send(SearchContract.Effect.NavigateToSelection(query))
                    }
                }
            }
            SearchContract.Intent.OnBackClicked -> {
                viewModelScope.launch { _effect.send(SearchContract.Effect.NavigateBack) }
            }
        }
    }

    private companion object {
        const val KEY_QUERY = "query"
    }
}