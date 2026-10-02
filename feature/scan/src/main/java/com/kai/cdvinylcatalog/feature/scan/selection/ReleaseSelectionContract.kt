package com.kai.cdvinylcatalog.feature.scan.selection

import com.kai.cdvinylcatalog.core.model.Release

object ReleaseSelectionContract {

    data class State(
        val results: List<Release> = emptyList(),
        val source: Source = Source.SEARCH,  // откуда пришли
        val inCollectionIds: Set<Long> = emptySet()
    ) {
        val isEmpty: Boolean
            get() = results.isEmpty()
    }

    enum class Source {
        SCAN,    // из сканера
        SEARCH   // из ручного поиска
    }

    sealed class Intent {
        data object OnBackClicked : Intent()
        data class OnResultClicked(val release: Release) : Intent()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class NavigateToDetails(val releaseId: Long) : Effect()
    }
}