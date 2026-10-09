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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.kai.cdvinylcatalog.core.database.SortOrder
import com.kai.cdvinylcatalog.core.model.CollectionItem
import com.kai.cdvinylcatalog.core.ui.CdVinylCatalogTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    state: CollectionContract.State,
    onIntent: (CollectionContract.Intent) -> Unit,
    onItemClicked: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Моя коллекция (${state.items.size})") },
                navigationIcon = {
                    IconButton(onClick = {
                        onIntent(CollectionContract.Intent.OnBackClicked)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Меню сортировки
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Сортировка")
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
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Сканировать")
                    }
                    IconButton(onClick = {
                        onIntent(CollectionContract.Intent.OnSearchClicked)
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Поиск")
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
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.isEmpty -> {
                    EmptyCollection(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            CollectionItemCard(
                                item = item,
                                onClick = { onItemClicked(item.id) },
                                onDelete = { onIntent(CollectionContract.Intent.OnDeleteItem(item)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SortOrder.label(): String = when (this) {
    SortOrder.ADDED_DESC -> "Сначала новые"
    SortOrder.ADDED_ASC -> "Сначала старые"
    SortOrder.ARTIST_ASC -> "По исполнителю (A→Z)"
    SortOrder.YEAR_DESC -> "По году (новые)"
}

@Composable
private fun CollectionItemCard(
    item: CollectionItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Обложка
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

            // Текст
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
                        text = "Год: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Формат: ${item.format.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                item.notes?.let {
                    Text(
                        text = "Заметка: $it",
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
                            text = pluralizeCopies(item.copyCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun pluralizeCopies(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    val word = when {
        mod100 in 11..19 -> "копий"
        mod10 == 1 -> "копия"
        mod10 in 2..4 -> "копии"
        else -> "копий"
    }
    return "$count $word"
}

@Composable
private fun EmptyCollection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Коллекция пуста",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Отсканируйте первый диск",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CollectionScreenPreview() {
    CdVinylCatalogTheme {
        CollectionScreen(
            state = CollectionContract.State(
                isLoading = false,
                items = emptyList()
            ),
            onIntent = {},
            onItemClicked = {}
        )
    }
}