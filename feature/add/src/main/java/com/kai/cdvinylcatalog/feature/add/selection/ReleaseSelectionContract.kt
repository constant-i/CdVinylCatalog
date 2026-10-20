package com.kai.cdvinylcatalog.feature.add.selection

import com.kai.cdvinylcatalog.domain.catalog.Release

object ReleaseSelectionContract {
    data class State(
        val source: Source = Source.SEARCH,
        /** ID релизов Discogs, которые уже есть в коллекции пользователя */
        val inCollectionIds: Set<Long> = emptySet(),
    )

    enum class Source { SCAN, SEARCH }

    sealed class Intent {
        data object OnBackClicked : Intent()
        data class OnResultClicked(val release: Release) : Intent()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class NavigateToDetails(val releaseId: Long) : Effect()
    }
}