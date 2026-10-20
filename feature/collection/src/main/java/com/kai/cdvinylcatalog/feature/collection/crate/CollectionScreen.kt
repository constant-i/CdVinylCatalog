package com.kai.cdvinylcatalog.feature.collection.crate

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.SubcomposeAsyncImage
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.collection.CollectionItem
import com.kai.cdvinylcatalog.domain.collection.SortOrder
import com.kai.cdvinylcatalog.feature.collection.R
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    state: CollectionContract.State,
    pagingItems: LazyPagingItems<CollectionItem>, // <-- добавили
    onIntent: (CollectionContract.Intent) -> Unit,
    onItemClicked: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.collection_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        onIntent(CollectionContract.Intent.OnBackClicked)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    // Меню сортировки
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.cd_sort)
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order.label()) },
                                onClick = {
                                    onIntent(CollectionContract.Intent.OnSortChanged(order))
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (state.sortOrder == order) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }

                    IconButton(onClick = {
                        onIntent(CollectionContract.Intent.OnScanClicked)
                    }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = stringResource(R.string.cd_scan)
                        )
                    }
                    IconButton(onClick = {
                        onIntent(CollectionContract.Intent.OnSearchClicked)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.cd_search))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Логика загрузки и пустоты для Paging
            when {
                pagingItems.loadState.refresh is LoadState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.itemCount == 0 -> {
                    EmptyCollection(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            count = pagingItems.itemCount,
                            key = { index -> pagingItems[index]?.id ?: index }
                        ) { index ->
                            val item = pagingItems[index]
                            if (item != null) {
                                CollectionItemCard(
                                    item = item,
                                    onClick = { onItemClicked(item.id) }
                                )
                            }
                        }

                        if (pagingItems.loadState.append is LoadState.Loading) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SortOrder.label(): String = when (this) {
    SortOrder.ADDED_DESC -> stringResource(R.string.sort_added_desc)
    SortOrder.ADDED_ASC -> stringResource(R.string.sort_added_asc)
    SortOrder.ARTIST_ASC -> stringResource(R.string.sort_artist_asc)
    SortOrder.YEAR_DESC -> stringResource(R.string.sort_year_desc)
}

@Composable
private fun CollectionItemCard(
    item: CollectionItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SubcomposeAsyncImage(
                    model = item.release.coverImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(20.dp))
                        }
                    },
                    error = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = item.release.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = item.release.artist,
                    style = MaterialTheme.typography.bodyMedium,
                )
                item.release.year?.let {
                    Text(
                        text = stringResource(R.string.collection_label_year, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(R.string.collection_label_format, item.format.name),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                item.notes?.let {
                    Text(
                        text = stringResource(R.string.collection_label_note, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (item.copyCount > 1) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = pluralStringResource(
                                R.plurals.collection_copies,
                                item.copyCount,
                                item.copyCount
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCollection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.collection_empty),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(R.string.collection_empty_hint),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private val mockRelease1 = Release(
    id = 1L, title = "Nevermind", artist = "Nirvana", year = 1991,
    barcode = null, coverImageUrl = null, imageUrls = emptyList(),
    label = "DGC", rawFormat = "CD", country = "US", releaseDate = null,
    catalogNumber = null, notes = null, tracklist = emptyList()
)

private val mockItem1 = CollectionItem(
    id = 1L, release = mockRelease1, format = Format.CD,
    addedAt = System.currentTimeMillis(), notes = "Фирменный диск", copyCount = 2
)

private val mockItem2 = CollectionItem(
    id = 2L, release = mockRelease1.copy(id = 2, title = "In Utero"),
    format = Format.CD, addedAt = System.currentTimeMillis(), notes = null, copyCount = 1
)

@Preview(name = "Light - С данными", showBackground = true, locale = "ru")
@Preview(name = "Dark - С данными", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "ru")
@Composable
private fun CollectionScreenPreview() {
    val mockItems = listOf(mockItem1, mockItem2)

    // MutableStateFlow + remember: без этого превью рисует первый кадр,
    // когда PagingData ещё не успел эмитить данные
    val mockPagingItems = remember {
        MutableStateFlow(PagingData.from(mockItems))
    }.collectAsLazyPagingItems()

    CdVinylCatalogTheme {
        CollectionScreen(
            state = CollectionContract.State(),
            pagingItems = mockPagingItems,
            onIntent = {},
            onItemClicked = {}
        )
    }
}

@Preview(name = "Light - Пустая коллекция", showBackground = true, locale = "ru")
@Composable
private fun CollectionScreenEmptyPreview() {
    val mockPagingItems = remember {
        MutableStateFlow(PagingData.from(emptyList<CollectionItem>()))
    }.collectAsLazyPagingItems()

    CdVinylCatalogTheme {
        CollectionScreen(
            state = CollectionContract.State(),
            pagingItems = mockPagingItems,
            onIntent = {},
            onItemClicked = {}
        )
    }
}