package com.kai.cdvinylcatalog.common.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.domain.catalog.Track

/**
 * Одна позиция треклиста: номер, название, длительность.
 *
 * Длительность необязательна — у части треков Discogs её не отдаёт.
 */
@Composable
fun TrackRow(
    track: Track,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = track.position,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        track.duration?.let { duration ->
            Text(text = duration, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(name = "Треки", showBackground = true, widthDp = 340)
@Composable
private fun TrackRowPreview() {
    CdVinylCatalogTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            TrackRow(Track(position = "1", title = "Dark Chest Of Wonders", duration = "4:28"))
            TrackRow(Track(position = "2", title = "Wish I Had An Angel", duration = "4:03"))
            // Без длительности правая часть просто не рисуется
            TrackRow(Track(position = "3", title = "Nemo", duration = null))
        }
    }
}
