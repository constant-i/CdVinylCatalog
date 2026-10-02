package com.kai.cdvinylcatalog.feature.scan.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.kai.cdvinylcatalog.core.model.Release

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
                items(state.results, key = { it.id }) { release ->
                    ReleaseCard(
                        release = release,
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
private fun ReleaseCard(release: Release, onClick: () -> Unit) {
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

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    release.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    release.artist,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                InfoRow("Формат", buildString {
                    release.year?.let { append(it) }
                    release.rawFormat?.let {
                        if (isNotEmpty()) append(" · ")
                        append(it)
                    }
                    if (isEmpty()) append("N/A")
                })
                InfoRow("Лейбл", release.label ?: "N/A", maxLines = 1)
                InfoRow("Страна", release.country ?: "N/A")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, maxLines: Int = 2) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/*
*
* @Composable
private fun SearchResultCard(
    release: Release,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = release.artist,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(2.dp))
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
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            style = style,
            color = color,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}
*
* */

//Column(modifier = Modifier.weight(1f)) {
//    Text(
//        text = release.artist,
//        style = MaterialTheme.typography.titleSmall
//    )
//    Spacer(modifier = Modifier.height(2.dp))
//    Text(
//        text = release.title,
//        style = MaterialTheme.typography.titleMedium
//    )
//    Spacer(modifier = Modifier.height(4.dp))
//    InfoRow(
//        label = "Формат",
//        value = release.rawFormat ?: "N/A"
//    )
//    InfoRow(
//        label = "Год",
//        value = release.year?.toString() ?: "N/A"
//    )
//    InfoRow(
//        label = "Лейбл",
//        value = release.label ?: "N/A",
//    )
//    InfoRow(
//        label = "Страна",
//        value = release.country ?: "N/A"
//    )
//}


//@Composable
//private fun InfoRow(
//    label: String,
//    value: String,
//    style: TextStyle = MaterialTheme.typography.bodySmall,
//    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
//    maxLines: Int = 3
//) {
//    Row(modifier = Modifier.fillMaxWidth()) {
//        Text(
//            text = "$label: ",
//            style = MaterialTheme.typography.bodySmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//        Text(
//            text = value,
//            style = style,
//            color = color,
//            maxLines = maxLines,
//            overflow = TextOverflow.Ellipsis
//        )
//    }
//}