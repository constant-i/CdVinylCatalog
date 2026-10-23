package com.kai.cdvinylcatalog.common.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme

/**
 * Заголовок блока релиза: название и исполнитель.
 *
 * Общий для экрана релиза из Discogs и экрана записи коллекции.
 */
@Composable
fun ReleaseHeader(
    title: String,
    artist: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = artist,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Заголовок", showBackground = true, widthDp = 360)
@Composable
private fun ReleaseHeaderPreview() {
    CdVinylCatalogTheme {
        ReleaseHeader(
            title = "Once",
            artist = "Nightwish",
            modifier = Modifier.padding(16.dp),
        )
    }
}
