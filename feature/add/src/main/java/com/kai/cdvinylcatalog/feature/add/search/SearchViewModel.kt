package com.kai.cdvinylcatalog.feature.add.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kai.cdvinylcatalog.core.model.UiMessage
import com.kai.cdvinylcatalog.domain.result.Outcome
import com.kai.cdvinylcatalog.feature.add.domain.SearchByTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchByText: SearchByTextUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchContract.State())
    val state: StateFlow<SearchContract.State> = _state.asStateFlow()

    private val _effect = Channel<SearchContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: SearchContract.Intent) {
        when (intent) {
            is SearchContract.Intent.OnQueryChanged -> onQueryChanged(intent.query)
            SearchContract.Intent.OnSearchClicked -> onSearch()
            SearchContract.Intent.OnBackClicked -> onBack()
        }
    }

    private fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
    }

    private fun onSearch() {
        val query = _state.value.query
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val outcome = searchByText(query)) {
                is Outcome.Success -> {
                    _state.update { it.copy(isLoading = false, hasSearched = true) }
                    if (outcome.data.isNotEmpty()) {
                        _effect.send(SearchContract.Effect.NavigateToSelection(outcome.data))
                    }
                }
                is Outcome.Failure -> {
                    _state.update {
                        it.copy(isLoading = false, hasSearched = true, error = outcome.error)
                    }
                    _effect.send(SearchContract.Effect.ShowToast(UiMessage.Error(outcome.error)))
                }
            }
        }
    }

    private fun onBack() {
        viewModelScope.launch {
            _effect.send(SearchContract.Effect.NavigateBack)
        }
    }
}