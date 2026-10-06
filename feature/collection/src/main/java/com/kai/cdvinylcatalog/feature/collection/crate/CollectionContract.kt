package com.kai.cdvinylcatalog.feature.collection.crate

import com.kai.cdvinylcatalog.core.database.SortOrder
import com.kai.cdvinylcatalog.core.model.CollectionItem

object CollectionContract {

    data class State(
        val isLoading: Boolean = true,
        val items: List<CollectionItem> = emptyList(),
        val sortOrder: SortOrder = SortOrder.ADDED_DESC,
        val error: String? = null
    ) {
        val isEmpty: Boolean
            get() = !isLoading && items.isEmpty()
    }

    sealed class Intent {
        data object OnBackClicked : Intent()
        data class OnDeleteItem(val item: CollectionItem) : Intent()
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