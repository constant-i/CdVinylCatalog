package com.kai.cdvinylcatalog.feature.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kai.cdvinylcatalog.core.model.CollectionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    state: CollectionContract.State,
    onIntent: (CollectionContract.Intent) -> Unit,
    onItemClicked: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Моя коллекция (${state.items.size})") },
                navigationIcon = {
                    IconButton(onClick = { onIntent(CollectionContract.Intent.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
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
private fun CollectionItemCard(
    item: CollectionItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.release.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = item.release.artist,
                style = MaterialTheme.typography.bodyMedium
            )
            item.release.year?.let {
                Text(
                    text = "Год: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Формат: ${item.format.name} · Копий: ${item.quantity}",
                style = MaterialTheme.typography.bodySmall
            )
            item.notes?.let {
                Text(
                    text = "Заметка: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
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
            text = "Коллекция пуста",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Отсканируйте первый диск",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}