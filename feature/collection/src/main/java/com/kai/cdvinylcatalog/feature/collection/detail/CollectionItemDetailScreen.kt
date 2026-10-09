package com.kai.cdvinylcatalog.feature.collection.detail

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kai.cdvinylcatalog.core.model.CollectionItem
import com.kai.cdvinylcatalog.core.model.Format
import com.kai.cdvinylcatalog.core.model.Release
import com.kai.cdvinylcatalog.core.model.Track
import com.kai.cdvinylcatalog.core.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.feature.collection.detail.CollectionItemDetailContract.State

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CollectionItemDetailScreen(
    state: State,
    onIntent: (CollectionItemDetailContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.displayRelease?.title ?: "Детали",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onIntent(CollectionItemDetailContract.Intent.OnBackClicked) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onIntent(CollectionItemDetailContract.Intent.OnEditClicked) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать"
                        )
                    }
                    IconButton(
                        onClick = { onIntent(CollectionItemDetailContract.Intent.OnDeleteClicked) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить"
                        )
                    }
                }
            )
        }
    ) { padding ->
        val item = state.collectionItem
        val release = state.displayRelease

        if (item == null || release == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            CollectionItemImageGallery(
                images = state.displayImages,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Название и исполнитель
            Text(text = release.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = release.artist, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Метаданные
            MetadataRow("Год", release.year?.toString())
            MetadataRow("Формат", release.rawFormat)
            MetadataRow("Лейбл", release.label)
            MetadataRow("Страна", release.country)
            MetadataRow("Catalog#", release.catalogNumber)
            item.notes?.let { MetadataRow("Заметка", it) }

            Spacer(modifier = Modifier.height(24.dp))

            // Треклист
            if (state.isLoadingDetails) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (state.tracklist.isNotEmpty()) {
                Text(text = "Треклист", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                state.tracklist.forEach { track ->
                    TrackRow(track)
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "$label: ", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(100.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TrackRow(track: Track) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = track.position,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(40.dp)
        )
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        track.duration?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CollectionItemDetailScreenPreview() {
    CdVinylCatalogTheme {
        CollectionItemDetailScreen(
            state = State(
                isLoadingDetails = false,
                detailedRelease = Release(
                    id = 1,
                    title = "Nevermind",
                    artist = "Nirvana",
                    year = 1991,
                    barcode = null,
                    coverImageUrl = null,
                    label = "DGC",
                    rawFormat = "CD, Album",
                    country = "US",
                    releaseDate = null,
                    catalogNumber = null,
                    notes = null,
                    tracklist = listOf(
                        Track("1", "Smells Like Teen Spirit", "5:01"),
                        Track("2", "In Bloom", "4:14")
                    )
                ),
                collectionItem = CollectionItem(
                    id = 1,
                    release = Release(
                        id = 1,
                        title = "Nevermind",
                        artist = "Nirvana",
                        year = 1991,
                        barcode = null,
                        coverImageUrl = null,
                        label = "DGC",
                        rawFormat = "CD, Album",
                        country = "US",
                        releaseDate = null,
                        catalogNumber = null,
                        notes = null,
                        tracklist = listOf(
                            Track("1", "Smells Like Teen Spirit", "5:01"),
                            Track("2", "In Bloom", "4:14")
                        )
                    ),
                    format = Format.CD,
                    addedAt = 0,
                    notes = "Фирменный диск",
                )
            ),
            onIntent = {}

        )
    }
}