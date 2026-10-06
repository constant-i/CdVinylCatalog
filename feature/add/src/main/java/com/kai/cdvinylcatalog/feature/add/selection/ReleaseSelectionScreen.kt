package com.kai.cdvinylcatalog.feature.add.selection

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.kai.cdvinylcatalog.core.model.Release
import com.kai.cdvinylcatalog.core.ui.CdVinylCatalogTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseSelectionScreen(
    state: ReleaseSelectionContract.State,
    onIntent: (ReleaseSelectionContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (state.source) {
        ReleaseSelectionContract.Source.SCAN -> "Найдено изданий (${state.results.size})"
        ReleaseSelectionContract.Source.SEARCH -> "Результаты поиска (${state.results.size})"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = {
                        onIntent(ReleaseSelectionContract.Intent.OnBackClicked)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (state.results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Ничего не найдено")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    state.results.sortedBy { it.id in state.inCollectionIds },  // false < true → новые сверху
                    key = { it.id }
                ) { release ->
                    ReleaseCard(
                        release = release,
                        isInCollection = release.id in state.inCollectionIds,
                        onClick = {
                            onIntent(ReleaseSelectionContract.Intent.OnResultClicked(release))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReleaseCard(
    release: Release,
    isInCollection: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SubcomposeAsyncImage(
                    model = release.coverImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                        }
                    },
                    error = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = release.artist,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = release.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                InfoRow(
                    label = "Формат",
                    value = release.rawFormat ?: "N/A"
                )
                InfoRow(
                    label = "Год",
                    value = release.year?.toString() ?: "N/A"
                )
                InfoRow(
                    label = "Лейбл",
                    value = release.label ?: "N/A",
                )
                InfoRow(
                    label = "Страна",
                    value = release.country ?: "N/A"
                )
                if (isInCollection) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Уже в коллекции",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    maxLines: Int = 3
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReleaseSelectionScreenPreview() {
    CdVinylCatalogTheme {
        ReleaseSelectionScreen(
            state = ReleaseSelectionContract.State(
                results = listOf(
                    Release(
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
                        notes = null
                    )
                )
            ),
            onIntent = {}
        )
    }
}