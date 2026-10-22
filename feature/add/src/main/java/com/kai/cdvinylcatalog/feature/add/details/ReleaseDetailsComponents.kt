package com.kai.cdvinylcatalog.feature.add.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
 * Строка «метка: значение» для блока метаданных.
 *
 * Пустое значение не показывается: у релиза из Discogs часто нет лейбла,
 * страны или номера каталога, и пустая строка только шумит.
 */
@Composable
internal fun MetadataRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return

    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(100.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Одна позиция треклиста: позиция, название, длительность.
 */
@Composable
internal fun TrackRow(track: Track) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = track.position,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(40.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Preview(name = "Строки метаданных и треков", showBackground = true, widthDp = 360)
@Composable
private fun ReleaseDetailsRowsPreview() {
    CdVinylCatalogTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MetadataRow(label = "Год", value = "2004")
            MetadataRow(label = "Лейбл", value = "Nuclear Blast")
            // Пустое значение не рисуется — это и проверяет превью
            MetadataRow(label = "Страна", value = null)
            Spacer(modifier = Modifier.height(12.dp))
            TrackRow(Track(position = "1", title = "Dark Chest Of Wonders", duration = "4:28"))
            TrackRow(Track(position = "2", title = "Wish I Had An Angel", duration = "4:03"))
            // Без длительности — правая часть просто не рисуется
            TrackRow(Track(position = "3", title = "Nemo", duration = null))
        }
    }
}
