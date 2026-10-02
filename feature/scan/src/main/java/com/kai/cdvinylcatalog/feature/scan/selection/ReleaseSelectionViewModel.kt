package com.kai.cdvinylcatalog.feature.scan.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kai.cdvinylcatalog.core.model.Release
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
class ReleaseSelectionViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ReleaseSelectionContract.State())
    val state: StateFlow<ReleaseSelectionContract.State> = _state.asStateFlow()

    private val _effect = Channel<ReleaseSelectionContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun setResults(releases: List<Release>, source: ReleaseSelectionContract.Source) {
        _state.update { it.copy(results = releases, source = source) }
    }

    fun onIntent(intent: ReleaseSelectionContract.Intent) {
        when (intent) {
            ReleaseSelectionContract.Intent.OnBackClicked -> onBack()
            is ReleaseSelectionContract.Intent.OnResultClicked -> onResultClicked(intent.release)
        }
    }

    private fun onBack() {
        viewModelScope.launch {
            _effect.send(ReleaseSelectionContract.Effect.NavigateBack)
        }
    }

    private fun onResultClicked(release: Release) {
        viewModelScope.launch {
            _effect.send(ReleaseSelectionContract.Effect.NavigateToDetails(release.id))
        }
    }
}