package com.kai.cdvinylcatalog.feature.collection.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kai.cdvinylcatalog.core.database.CollectionRepository
import com.kai.cdvinylcatalog.feature.collection.domain.GetReleaseDetailsUseCase
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
class CollectionItemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CollectionRepository,
    private val getReleaseDetails: GetReleaseDetailsUseCase
) : ViewModel() {

    private val itemId: Long = savedStateHandle.get<Long>("itemId") ?: -1L

    private val _state = MutableStateFlow(CollectionItemDetailContract.State())
    val state: StateFlow<CollectionItemDetailContract.State> = _state.asStateFlow()

    private val _effect = Channel<CollectionItemDetailContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadCollectionItem()
    }

    private fun loadCollectionItem() {
        viewModelScope.launch {
            val item = repository.getItemById(itemId)
            if (item == null) {
                _effect.send(CollectionItemDetailContract.Effect.ShowToast("Запись не найдена"))
                _effect.send(CollectionItemDetailContract.Effect.NavigateBack)
                return@launch
            }
            _state.update { it.copy(collectionItem = item) }
            onLoadDetails()  // Автоматически грузим треклист
        }
    }

    fun onIntent(intent: CollectionItemDetailContract.Intent) {
        when (intent) {
            CollectionItemDetailContract.Intent.OnBackClicked -> onBack()
            CollectionItemDetailContract.Intent.OnLoadDetails -> onLoadDetails()
            CollectionItemDetailContract.Intent.OnDeleteClicked -> onDelete()
            CollectionItemDetailContract.Intent.OnEditNotesClicked -> { /* TODO */ }
            is CollectionItemDetailContract.Intent.OnNotesChanged -> { /* TODO */ }
        }
    }

    private fun onLoadDetails() {
        val releaseId = _state.value.collectionItem?.release?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingDetails = true) }
            getReleaseDetails(releaseId)
                .onSuccess { release ->
                    _state.update { it.copy(isLoadingDetails = false, detailedRelease = release) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoadingDetails = false, error = e.message) }
                }
        }
    }

    private fun onBack() {
        viewModelScope.launch {
            _effect.send(CollectionItemDetailContract.Effect.NavigateBack)
        }
    }

    private fun onDelete() {
        val item = _state.value.collectionItem ?: return
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            repository.removeFromCollection(item.id)
            _effect.send(CollectionItemDetailContract.Effect.NavigateBack)
        }
    }
}