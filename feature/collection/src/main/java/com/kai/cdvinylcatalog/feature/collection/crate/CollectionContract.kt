package com.kai.cdvinylcatalog.feature.collection.crate

import com.kai.cdvinylcatalog.domain.collection.SortOrder
import com.kai.cdvinylcatalog.domain.collection.CollectionItem
import com.kai.cdvinylcatalog.domain.result.AppError

object CollectionContract {

    data class State(
        val sortOrder: SortOrder = SortOrder.ADDED_DESC,
        val error: AppError? = null
    )

    sealed class Intent {
        data object OnBackClicked : Intent()
        data object OnScanClicked : Intent()
        data object OnSearchClicked : Intent()
        data class OnSortChanged(val sortOrder: SortOrder) : Intent()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data object NavigateToScan : Effect()
        data object NavigateToSearch : Effect()
    }
}