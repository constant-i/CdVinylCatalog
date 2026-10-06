package com.kai.cdvinylcatalog.feature.collection.crate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kai.cdvinylcatalog.core.database.CollectionRepository
import com.kai.cdvinylcatalog.core.model.CollectionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val repository: CollectionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CollectionContract.State())
    val state: StateFlow<CollectionContract.State> = _state.asStateFlow()

    private val _effect = Channel<CollectionContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        observeCollection()
    }

    /**
     * Подписываемся на Flow из Room.
     * При изменении базы — список автоматически обновляется.
     */
    private fun observeCollection() {
        viewModelScope.launch {
            repository.getAllItems()
                .onEach { items ->
                    _state.update {
                        it.copy(isLoading = false, items = items, error = null)
                    }
                }
                .catch { e ->
                    _state.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
                .collect()
        }
    }

    fun onIntent(intent: CollectionContract.Intent) {
        when (intent) {
            CollectionContract.Intent.OnBackClicked -> onBack()
            is CollectionContract.Intent.OnDeleteItem -> onDeleteItem(intent.item)
            CollectionContract.Intent.OnScanClicked -> onScanClicked()
            CollectionContract.Intent.OnSearchClicked -> onSearchClicked()
        }
    }

    private fun onScanClicked() {
        viewModelScope.launch {
            _effect.send(CollectionContract.Effect.NavigateToScan)
        }
    }

    private fun onSearchClicked() {
        viewModelScope.launch {
            _effect.send(CollectionContract.Effect.NavigateToSearch)
        }
    }

    private fun onBack() {
        viewModelScope.launch {
            _effect.send(CollectionContract.Effect.NavigateBack)
        }
    }

    private fun onDeleteItem(item: CollectionItem) {
        viewModelScope.launch {
            repository.removeFromCollection(item.id)
        }
    }
}