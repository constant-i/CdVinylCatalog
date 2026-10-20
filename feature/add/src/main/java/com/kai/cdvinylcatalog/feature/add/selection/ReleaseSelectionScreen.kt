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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.MutableStateFlow
import coil.compose.SubcomposeAsyncImage
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.common.ui.asText
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.result.AppError
import com.kai.cdvinylcatalog.domain.result.AppErrorException
import com.kai.cdvinylcatalog.feature.add.R

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onIntent(ReleaseSelectionContract.Intent.OnBackClicked)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
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
                    label = stringResource(R.string.selection_label_format),
                    value = release.rawFormat
                )
                InfoRow(
                    label = stringResource(R.string.selection_label_year),
                    value = release.year?.toString()
                )
                InfoRow(
                    label = stringResource(R.string.selection_label_label),
                    value = release.label
                )
                InfoRow(
                    label = stringResource(R.string.selection_label_country),
                    value = release.country
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
                            text = stringResource(R.string.selection_already_in_collection),
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
    value: String?,
    maxLines: Int = 3
) {
    if (value.isNullOrBlank()) return
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