package com.kai.cdvinylcatalog.feature.add.search

import com.kai.cdvinylcatalog.common.ui.message.UiMessage
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.result.AppError

object SearchContract {

    data class State(
        val query: String = "",
        val isLoading: Boolean = false,
        val hasSearched: Boolean = false,
        val error: AppError? = null
    )

    sealed class Intent {
        data class OnQueryChanged(val query: String) : Intent()
        data object OnSearchClicked : Intent()
        data object OnBackClicked : Intent()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class ShowToast(val message: UiMessage) : Effect()
        data class NavigateToSelection(val results: List<Release>) : Effect()
    }
}