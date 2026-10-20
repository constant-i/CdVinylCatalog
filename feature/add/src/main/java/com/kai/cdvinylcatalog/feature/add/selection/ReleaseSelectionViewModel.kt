package com.kai.cdvinylcatalog.feature.add.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.feature.add.domain.ObserveCollectionReleaseIdsUseCase
import com.kai.cdvinylcatalog.feature.add.domain.SearchByTextUseCase
import com.kai.cdvinylcatalog.feature.add.domain.SearchReleaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReleaseSelectionViewModel @Inject constructor(
    private val searchByText: SearchByTextUseCase,
    private val searchByBarcode: SearchReleaseUseCase,
    private val observeCollectionReleaseIds: ObserveCollectionReleaseIdsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ReleaseSelectionContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<ReleaseSelectionContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        // Один реактивный запрос вместо проверки каждого элемента списка
        viewModelScope.launch {
            observeCollectionReleaseIds().collect { ids ->
                _state.update { it.copy(inCollectionIds = ids) }
            }
        }
    }

    private val searchParams = MutableStateFlow<Pair<String, ReleaseSelectionContract.Source>?>(null)

    val results: StateFlow<PagingData<Release>> = searchParams
        .filterNotNull()
        .flatMapLatest { (query, source) ->
            when (source) {
                ReleaseSelectionContract.Source.SCAN -> searchByBarcode(query)
                ReleaseSelectionContract.Source.SEARCH -> searchByText(query)
            }
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        )

    fun initSearch(query: String, source: ReleaseSelectionContract.Source) {
        _state.update { it.copy(source = source) }
        searchParams.value = query to source
    }

    fun onIntent(intent: ReleaseSelectionContract.Intent) {
        when (intent) {
            ReleaseSelectionContract.Intent.OnBackClicked -> {
                viewModelScope.launch { _effect.send(ReleaseSelectionContract.Effect.NavigateBack) }
            }
            is ReleaseSelectionContract.Intent.OnResultClicked -> {
                viewModelScope.launch { _effect.send(ReleaseSelectionContract.Effect.NavigateToDetails(intent.release.id)) }
            }
        }
    }
}