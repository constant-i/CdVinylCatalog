package com.kai.cdvinylcatalog.common.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.common.ui.R
import com.kai.cdvinylcatalog.domain.catalog.Track

/**
 * Секция треклиста: заголовок и строки треков в карточке.
 *
 * Ничего не рисует при пустом списке — вызывающему не нужно проверять самому.
 */
@Composable
fun TracklistSection(
    tracks: List<Track>,
    modifier: Modifier = Modifier,
) {
    if (tracks.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.disc_tracklist),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                tracks.forEach { track -> TrackRow(track) }
            }
        }
    }
}

@Preview(name = "Треклист", showBackground = true, widthDp = 360)
@Composable
private fun TracklistSectionPreview() {
    CdVinylCatalogTheme {
        TracklistSection(
            tracks = previewTracks,
            modifier = Modifier.padding(16.dp),
        )
    }
}

private val previewTracks = listOf(
    Track(position = "1", title = "Dark Chest Of Wonders", duration = "4:28"),
    Track(position = "2", title = "Wish I Had An Angel", duration = "4:03"),
    Track(position = "3", title = "Nemo", duration = null),
)
