package com.kai.cdvinylcatalog.feature.add.selection

import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.collection.CollectionItem

object ReleaseSelectionContract {

    data class State(
        val source: Source = Source.SEARCH,
        /** ID релизов Discogs, которые уже есть в коллекции пользователя */
        val inCollectionIds: Set<Long> = emptySet(),
        /**
         * Сети нет, Discogs недоступен — показываем результат из локальной базы.
         *
         * `null` — ещё не проверяли или сеть есть.
         * Пустой список — проверяли, ничего с таким штрихкодом нет.
         */
        val offlineMatches: List<CollectionItem>? = null,
        /** Идёт локальный поиск по штрихкоду. */
        val isLookingUpOffline: Boolean = false,
    ) {
        val isOffline: Boolean
            get() = offlineMatches != null
    }

    enum class Source { SCAN, SEARCH }

    sealed class Intent {
        data object OnBackClicked : Intent()
        data class OnResultClicked(val release: Release) : Intent()

        /** Открыть запись из локальной коллекции (офлайн-результат). */
        data class OnLocalItemClicked(val itemId: Long) : Intent()

        /**
         * Сеть недоступна — надо ответить по локальной базе.
         * Вызывается экраном при ошибке `NoConnection` на экране сканирования.
         */
        data object OnOfflineFallback : Intent()

        /** Повторить сетевой запрос: связь могла вернуться. */
        data object OnRetryOnline : Intent()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class NavigateToDetails(val releaseId: Long) : Effect()
        data class NavigateToLocalItem(val itemId: Long) : Effect()
    }
}
