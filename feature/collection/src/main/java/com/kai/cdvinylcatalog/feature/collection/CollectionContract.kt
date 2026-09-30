package com.kai.cdvinylcatalog.feature.collection

import com.kai.cdvinylcatalog.core.model.CollectionItem

object CollectionContract {

    data class State(
        val isLoading: Boolean = true,
        val items: List<CollectionItem> = emptyList(),
        val error: String? = null
    ) {
        val isEmpty: Boolean
            get() = !isLoading && items.isEmpty()
    }

    sealed class Intent {
        data object OnBackClicked : Intent()
        data class OnItemClicked(val item: CollectionItem) : Intent()
        data class OnDeleteItem(val item: CollectionItem) : Intent()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
    }
}