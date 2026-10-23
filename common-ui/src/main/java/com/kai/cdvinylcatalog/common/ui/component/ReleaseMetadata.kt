package com.kai.cdvinylcatalog.common.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.common.ui.R

/**
 * Метаданные релиза: год, формат, лейбл, страна, номер каталога, заметка.
 *
 * Одинаковые на обоих экранах деталей. Поле передано — строка рисуется,
 * `null` — пропускается.
 */
@Composable
fun ReleaseMetadata(
    modifier: Modifier = Modifier,
    year: String? = null,
    format: String? = null,
    label: String? = null,
    country: String? = null,
    catalogNumber: String? = null,
    note: String? = null,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            MetadataRow(stringResource(R.string.disc_label_year), year)
            MetadataRow(stringResource(R.string.disc_label_format), format)
            MetadataRow(stringResource(R.string.disc_label_label), label)
            MetadataRow(stringResource(R.string.disc_label_country), country)
            MetadataRow(stringResource(R.string.disc_label_catalog), catalogNumber)
            MetadataRow(stringResource(R.string.disc_label_note), note)
        }
    }
}

@Preview(name = "Метаданные", showBackground = true, widthDp = 360)
@Composable
private fun ReleaseMetadataPreview() {
    CdVinylCatalogTheme {
        ReleaseMetadata(
            modifier = Modifier.padding(16.dp),
            year = "2004",
            format = "CD, Album",
            label = "Nuclear Blast",
            country = "Europe",
            catalogNumber = "NB 1291-2",
        )
    }
}

@Preview(name = "С заметкой", showBackground = true, widthDp = 360)
@Composable
private fun ReleaseMetadataNotePreview() {
    CdVinylCatalogTheme {
        ReleaseMetadata(
            modifier = Modifier.padding(16.dp),
            year = "2007",
            format = "CD",
            label = "Irönd",
            country = "Россия",
            note = "подписан группой",
        )
    }
}
