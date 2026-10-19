package com.kai.cdvinylcatalog.feature.collection.crate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kai.cdvinylcatalog.domain.collection.CollectionItem
import com.kai.cdvinylcatalog.domain.collection.SortOrder
import com.kai.cdvinylcatalog.domain.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val repository: CollectionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CollectionContract.State())
    val state: StateFlow<CollectionContract.State> = _state.asStateFlow()

    private val _effect = Channel<CollectionContract.Effect>()
    val effect = _effect.receiveAsFlow()

    // Создаем Flow для PagingData, который реагирует на смену сортировки
    val items: StateFlow<PagingData<CollectionItem>> = _state
        .flatMapLatest { state ->
            repository.getItems(state.sortOrder)
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        )

    fun onIntent(intent: CollectionContract.Intent) {
        when (intent) {
            CollectionContract.Intent.OnBackClicked -> onBack()
            CollectionContract.Intent.OnScanClicked -> onScanClicked()
            CollectionContract.Intent.OnSearchClicked -> onSearchClicked()
            is CollectionContract.Intent.OnSortChanged -> onSortChanged(intent.sortOrder)
        }
    }

    private fun onSortChanged(sortOrder: SortOrder) {
        if (_state.value.sortOrder == sortOrder) return
        _state.update { it.copy(sortOrder = sortOrder) }
    }

    private fun onScanClicked() {
        viewModelScope.launch { _effect.send(CollectionContract.Effect.NavigateToScan) }
    }

    private fun onSearchClicked() {
        viewModelScope.launch { _effect.send(CollectionContract.Effect.NavigateToSearch) }
    }

    private fun onBack() {
        viewModelScope.launch { _effect.send(CollectionContract.Effect.NavigateBack) }
    }
}