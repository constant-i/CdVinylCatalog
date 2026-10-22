package com.kai.cdvinylcatalog.feature.add.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.result.Outcome
import com.kai.cdvinylcatalog.domain.usecase.catalog.SearchByTextUseCase
import com.kai.cdvinylcatalog.domain.usecase.catalog.SearchReleaseUseCase
import com.kai.cdvinylcatalog.domain.usecase.collection.FindItemsByBarcodeUseCase
import com.kai.cdvinylcatalog.domain.usecase.collection.ObserveCollectionReleaseIdsUseCase
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
    private val findItemsByBarcode: FindItemsByBarcodeUseCase,
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
            ReleaseSelectionContract.Intent.OnOfflineFallback -> onOfflineFallback()
            ReleaseSelectionContract.Intent.OnRetryOnline -> onRetryOnline()
            is ReleaseSelectionContract.Intent.OnLocalItemClicked -> {
                viewModelScope.launch {
                    _effect.send(
                        ReleaseSelectionContract.Effect.NavigateToLocalItem(intent.itemId)
                    )
                }
            }
        }
    }

    /**
     * Сети нет — ищем в локальной базе по штрихкоду.
     *
     * Работает только для сканирования: ручной поиск по названию локально
     * выполнить нельзя без полнотекстового индекса.
     */
    private fun onOfflineFallback() {
        val (barcode, source) = searchParams.value ?: return
        if (source != ReleaseSelectionContract.Source.SCAN) return
        if (_state.value.isOffline || _state.value.isLookingUpOffline) return

        // Флаг ставим синхронно: два интента подряд должны увидеть его
        // до того, как запустится корутина
        _state.update { it.copy(isLookingUpOffline = true) }

        viewModelScope.launch {
            val matches = when (val outcome = findItemsByBarcode(barcode)) {
                is Outcome.Success -> outcome.data
                is Outcome.Failure -> emptyList()
            }

            _state.update {
                it.copy(offlineMatches = matches, isLookingUpOffline = false)
            }
        }
    }

    /**
     * Связь могла вернуться — сбрасываем офлайн-состояние и перезапускаем поиск.
     */
    private fun onRetryOnline() {
        val params = searchParams.value ?: return
        _state.update { it.copy(offlineMatches = null) }
        // Повторная установка тех же параметров не эмитит — пересоздаём поток
        searchParams.value = null
        searchParams.value = params
    }
}