package com.kai.cdvinylcatalog.feature.add.selection

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.common.ui.asText
import com.kai.cdvinylcatalog.common.ui.component.CdTopBar
import com.kai.cdvinylcatalog.common.ui.component.DiscCard
import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.collection.CollectionItem
import com.kai.cdvinylcatalog.domain.result.AppError
import com.kai.cdvinylcatalog.domain.result.AppErrorException
import com.kai.cdvinylcatalog.feature.add.R
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

@Composable
fun ReleaseSelectionScreen(
    state: ReleaseSelectionContract.State,
    pagingItems: LazyPagingItems<Release>,
    onIntent: (ReleaseSelectionContract.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val title = when (state.source) {
        ReleaseSelectionContract.Source.SCAN ->
            stringResource(R.string.selection_title_scan)
        ReleaseSelectionContract.Source.SEARCH ->
            stringResource(R.string.selection_title_search)
    }

    val refreshError = (pagingItems.loadState.refresh as? LoadState.Error)
        ?.error
        ?.asAppError()

    // Сети нет — вместо ошибки отвечаем по локальной базе.
    // Только для сканирования: текстовый поиск локально выполнить нельзя.
    LaunchedEffect(refreshError, state.source) {
        if (refreshError == AppError.NoConnection &&
            state.source == ReleaseSelectionContract.Source.SCAN
        ) {
            onIntent(ReleaseSelectionContract.Intent.OnOfflineFallback)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CdTopBar(
                title = title,
                onBack = {
                    onIntent(ReleaseSelectionContract.Intent.OnBackClicked)
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLookingUpOffline -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.offlineMatches != null -> {
                    OfflineResult(
                        matches = state.offlineMatches,
                        onOpenItem = { itemId ->
                            onIntent(ReleaseSelectionContract.Intent.OnLocalItemClicked(itemId))
                        },
                        onRetry = { onIntent(ReleaseSelectionContract.Intent.OnRetryOnline) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                pagingItems.loadState.refresh is LoadState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                pagingItems.loadState.refresh is LoadState.Error -> {
                    val error = (pagingItems.loadState.refresh as LoadState.Error).error.asAppError()
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.selection_load_error),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = error.asText(context),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { pagingItems.retry() }) {
                            Text(stringResource(R.string.common_retry))
                        }
                    }
                }

                pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.itemCount == 0 -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.search_empty))
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            count = pagingItems.itemCount,
                            key = pagingItems.itemKey { it.id }
                        ) { index ->
                            val release = pagingItems[index]
                            if (release != null) {
                                ReleaseCard(
                                    release = release,
                                    isInCollection = release.id in state.inCollectionIds,
                                    onClick = {
                                        onIntent(ReleaseSelectionContract.Intent.OnResultClicked(release))
                                    },
                                )
                            }
                        }

                        if (pagingItems.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(Modifier.size(24.dp))
                                }
                            }
                        }

                        if (pagingItems.loadState.append is LoadState.Error) {
                            item {
                                TextButton(
                                    onClick = { pagingItems.retry() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.selection_append_error))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Ответ без сети: что удалось найти в локальной коллекции по штрихкоду.
 *
 * Формулировка важна: один штрихкод может соответствовать нескольким изданиям,
 * поэтому говорим «диск с таким штрихкодом», а не «этот релиз».
 */
@Composable
private fun OfflineResult(
    matches: List<CollectionItem>,
    onOpenItem: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.selection_offline_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))

        if (matches.isEmpty()) {
            Text(
                text = stringResource(R.string.selection_offline_not_found),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = offlineFoundText(matches.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            matches.forEach { item ->
                LocalItemCard(item = item, onClick = { onOpenItem(item.id) })
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.common_retry))
        }
    }
}

/**
 * Заголовок со количеством найденных копий.
 *
 * Для ровно одного диска — отдельная строка без числа: форма `one`
 * в русском выбирается и для 21, 31 и т.д., а «21 диск» в этом
 * предложении читалось бы странно.
 */
@Composable
private fun offlineFoundText(count: Int): String =
    if (count == 1) {
        stringResource(R.string.selection_offline_found_one)
    } else {
        pluralStringResource(R.plurals.selection_offline_found, count, count)
    }

/**
 * Компактная карточка записи из локальной коллекции.
 *
 * Фото показываются из файла: пользователь мог добавить свой буклет,
 * это поможет узнать диск.
 */
@Composable
private fun LocalItemCard(
    item: CollectionItem,
    onClick: () -> Unit,
) {
    val cover = item.release.coverImageUrl

    DiscCard(
        title = item.release.title,
        artist = item.release.artist,
        onClick = onClick,
        // Файл или URL — Coil понимает оба варианта
        cover = cover?.let { if (it.startsWith("http")) it else File(it) },
        coverSize = 56.dp,
        format = item.format.name,
        note = item.notes
    )
}

/**
 * Достаёт доменную ошибку из [LoadState.Error].
 * Presentation-слой знает только о [AppErrorException], но не о сетевых исключениях.
 */
private fun Throwable.asAppError(): AppError =
    (this as? AppErrorException)?.error ?: AppError.Unknown

@Composable
private fun ReleaseCard(
    release: Release,
    isInCollection: Boolean,
    onClick: () -> Unit
) {
    val badge: (@Composable () -> Unit)? = if (isInCollection) {
        { InCollectionBadge() }
    } else {
        null
    }

    DiscCard(
        title = release.title,
        artist = release.artist,
        onClick = onClick,
        cover = release.coverImageUrl,
        coverSize = 80.dp,
        format = release.rawFormat,
        year = release.year?.toString(),
        label = release.label,
        country = release.country,
        badge = badge,
    )
}

@Composable
private fun InCollectionBadge() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.selection_already_in_collection),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private val previewReleases = listOf(
    Release(
        id = 1L,
        title = "Nevermind",
        artist = "Nirvana",
        year = 1991,
        barcode = null,
        coverImageUrl = null,
        label = "DGC",
        rawFormat = "CD, Album",
        country = "US",
        releaseDate = null,
        catalogNumber = "DGCD-24425",
        notes = null
    ),
    Release(
        id = 2L,
        title = "The Dark Side of the Moon",
        artist = "Pink Floyd",
        year = 1973,
        barcode = null,
        coverImageUrl = null,
        label = "Harvest",
        rawFormat = "Vinyl, LP",
        country = "UK",
        releaseDate = null,
        catalogNumber = "SHVL 804",
        notes = null
    )
)

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReleaseSelectionScreenPreview() {
    val pagingItems = remember {
        MutableStateFlow(PagingData.from(previewReleases))
    }.collectAsLazyPagingItems()

    CdVinylCatalogTheme {
        ReleaseSelectionScreen(
            state = ReleaseSelectionContract.State(),
            pagingItems = pagingItems,
            onIntent = {}
        )
    }
}

@Preview(name = "Офлайн — найдено", showBackground = true, locale = "ru")
@Composable
private fun ReleaseSelectionOfflineFoundPreview() {
    val pagingItems = remember {
        MutableStateFlow(PagingData.empty<Release>())
    }.collectAsLazyPagingItems()

    CdVinylCatalogTheme {
        ReleaseSelectionScreen(
            state = ReleaseSelectionContract.State(
                source = ReleaseSelectionContract.Source.SCAN,
                offlineMatches = listOf(previewLocalItem())
            ),
            pagingItems = pagingItems,
            onIntent = {}
        )
    }
}

@Preview(name = "Офлайн — несколько копий", showBackground = true, locale = "ru")
@Composable
private fun ReleaseSelectionOfflineManyPreview() {
    val pagingItems = remember {
        MutableStateFlow(PagingData.empty<Release>())
    }.collectAsLazyPagingItems()

    CdVinylCatalogTheme {
        ReleaseSelectionScreen(
            state = ReleaseSelectionContract.State(
                source = ReleaseSelectionContract.Source.SCAN,
                offlineMatches = listOf(
                    previewLocalItem(id = 1L),
                    previewLocalItem(id = 2L, format = Format.VINYL)
                )
            ),
            pagingItems = pagingItems,
            onIntent = {}
        )
    }
}

private fun previewLocalItem(
    id: Long = 1L,
    format: Format = Format.CD
) = CollectionItem(
    id = id,
    release = Release(
        id = 0L,
        title = "Once",
        artist = "Nightwish",
        year = 2004,
        barcode = "727361129125",
        coverImageUrl = null,
        label = "Nuclear Blast",
        rawFormat = "CD",
        country = "Europe",
        releaseDate = null,
        catalogNumber = null,
        notes = null
    ),
    format = format,
    addedAt = 0L,
    notes = null
)

@Preview(name = "Офлайн — не найдено", showBackground = true, locale = "ru")
@Composable
private fun ReleaseSelectionOfflineEmptyPreview() {
    val pagingItems = remember {
        MutableStateFlow(PagingData.empty<Release>())
    }.collectAsLazyPagingItems()

    CdVinylCatalogTheme {
        ReleaseSelectionScreen(
            state = ReleaseSelectionContract.State(
                source = ReleaseSelectionContract.Source.SCAN,
                offlineMatches = emptyList()
            ),
            pagingItems = pagingItems,
            onIntent = {}
        )
    }
}