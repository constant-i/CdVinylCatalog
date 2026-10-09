package com.kai.cdvinylcatalog.feature.collection.detail

import com.kai.cdvinylcatalog.core.model.CollectionItem
import com.kai.cdvinylcatalog.core.model.Release
import com.kai.cdvinylcatalog.core.model.Track

object CollectionItemDetailContract {

    data class State(
        val collectionItem: CollectionItem? = null,
        val detailedRelease: Release? = null,
        val displayRelease: Release? = null,
        val isLoadingDetails: Boolean = false,
        val isDeleting: Boolean = false,
        val error: String? = null
    ) {
        val tracklist: List<Track>
            get() = displayRelease?.tracklist ?: emptyList()

        val displayImages: List<String>
            get() = displayRelease?.imageUrls ?: emptyList()
    }

    sealed class Intent {
        data object OnBackClicked : Intent()
        data object OnDeleteClicked : Intent()
        data object OnEditClicked : Intent()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class ShowToast(val message: String) : Effect()
        data class NavigateToEdit(val itemId: Long) : Effect()
    }
}