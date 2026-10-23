package com.kai.cdvinylcatalog.common.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme

/**
 * Строка «метка: значение».
 *
 * Пустое значение не рисуется: у релиза из Discogs часто нет лейбла, страны
 * или номера каталога, и пустая строка только шумит.
 *
 * @param labelWidth ширина колонки метки. `null` — метка и значение идут
 *   подряд, без выравнивания по колонкам (компактный вариант для карточек).
 */
@Composable
fun MetadataRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    labelWidth: Dp? = 100.dp,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = LocalTextStyle.current.color,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    if (value.isNullOrBlank()) return

    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = style,
            color = labelColor,
            modifier = if (labelWidth != null) Modifier.width(labelWidth) else Modifier,
        )
        Text(
            text = value,
            style = style,
            color = valueColor,
            modifier = Modifier.weight(1f),
            maxLines = maxLines,
            overflow = overflow,
        )
    }
}

@Preview(name = "С значением и без", showBackground = true, widthDp = 340)
@Composable
private fun MetadataRowPreview() {
    CdVinylCatalogTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            MetadataRow(label = "Год", value = "2004")
            MetadataRow(label = "Лейбл", value = "Nuclear Blast")
            // Пустое значение — строки нет вовсе
            MetadataRow(label = "Страна", value = null)
        }
    }
}
