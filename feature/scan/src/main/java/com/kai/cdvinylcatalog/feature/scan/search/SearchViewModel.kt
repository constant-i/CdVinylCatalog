package com.kai.cdvinylcatalog.feature.scan.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kai.cdvinylcatalog.core.model.Format
import com.kai.cdvinylcatalog.core.model.Release
import com.kai.cdvinylcatalog.core.model.parseFormat
import com.kai.cdvinylcatalog.feature.scan.domain.AddToCollectionUseCase
import com.kai.cdvinylcatalog.feature.scan.domain.CheckCollectionUseCase
import com.kai.cdvinylcatalog.feature.scan.domain.SearchByTextUseCase
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
    private val searchByText: SearchByTextUseCase,
    private val checkCollection: CheckCollectionUseCase,
    private val addToCollection: AddToCollectionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchContract.State())
    val state: StateFlow<SearchContract.State> = _state.asStateFlow()

    private val _effect = Channel<SearchContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: SearchContract.Intent) {
        when (intent) {
            is SearchContract.Intent.OnQueryChanged -> onQueryChanged(intent.query)
            SearchContract.Intent.OnSearchClicked -> onSearch()
            is SearchContract.Intent.OnResultClicked -> onResultClicked(intent.release)
            is SearchContract.Intent.OnFormatSelected -> onFormatSelected(intent.format)
            SearchContract.Intent.OnFormatPickerDismissed -> onFormatPickerDismissed()
            SearchContract.Intent.OnConfirmAdd -> onConfirmAdd()
            SearchContract.Intent.OnBackClicked -> onBack()
            is SearchContract.Intent.OnAddAnotherConfirmed -> onAddAnotherConfirmed(intent.release)
            SearchContract.Intent.OnAddAnotherDismissed -> onAddAnotherDismissed()
        }
    }

    private fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
    }

    private fun onSearch() {
        val query = _state.value.query
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            searchByText(query)
                .onSuccess { releases ->
                    _state.update {
                        it.copy(isLoading = false, results = releases, hasSearched = true)
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isLoading = false, error = e.message, hasSearched = true)
                    }
                }
        }
    }

    private fun onResultClicked(release: Release) {
        viewModelScope.launch {
            val inCollection = checkCollection(release.id)
            val detectedFormat = parseFormat(release.rawFormat)

            if (inCollection) {
                _state.update { it.copy(askAddAnother = release) }
                return@launch
            }

            if (detectedFormat != Format.UNKNOWN) {
                addRelease(release, detectedFormat)
            } else {
                _state.update {
                    it.copy(
                        selectedRelease = release,
                        selectedFormat = Format.CD,
                        showFormatPicker = true
                    )
                }
            }
        }
    }

    private fun addRelease(release: Release, format: Format) {
        viewModelScope.launch {
            try {
                addToCollection(release, format)
                _effect.send(SearchContract.Effect.ShowToast("Добавлено: ${release.title}"))
            } catch (e: Exception) {
                _effect.send(SearchContract.Effect.ShowToast("Ошибка: ${e.message}"))
            }
        }
    }

    private fun onAddAnotherConfirmed(release: Release) {
        val format = parseFormat(release.rawFormat).takeIf { it != Format.UNKNOWN }
            ?: _state.value.selectedFormat
        _state.update { it.copy(askAddAnother = null) }
        addRelease(release, format)
    }

    private fun onAddAnotherDismissed() {
        _state.update { it.copy(askAddAnother = null) }
    }

    private fun onFormatSelected(format: Format) {
        _state.update { it.copy(selectedFormat = format) }
    }

    private fun onFormatPickerDismissed() {
        _state.update { it.copy(selectedRelease = null, showFormatPicker = false) }
    }

    private fun onConfirmAdd() {
        val release = _state.value.selectedRelease ?: return
        val format = _state.value.selectedFormat
        _state.update { it.copy(selectedRelease = null, showFormatPicker = false) }
        addRelease(release, format)
    }

    private fun onBack() {
        viewModelScope.launch {
            _effect.send(SearchContract.Effect.NavigateBack)
        }
    }

    fun setInitialResults(releases: List<Release>) {
        _state.update {
            it.copy(
                results = releases,
                hasSearched = true,
                query = ""
            )
        }
    }
}