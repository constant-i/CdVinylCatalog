package com.kai.cdvinylcatalog.common.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.common.ui.R

/**
 * Карточка диска в списке.
 *
 * Одна на все списки: результаты поиска в Discogs, локальная коллекция,
 * офлайн-результат сканирования.
 *
 * Правило простое: **поле передано — строка рисуется, `null` — не рисуется**.
 * Никаких флагов `showLabel`/`showCountry`: набор полей и есть настройка.
 *
 * @param cover модель для Coil: URL, файл или `null`
 * @param coverSize размер обложки; в поиске крупнее, в коллекции компактнее
 * @param badge дополнительный бейдж под метаданными («уже в коллекции»,
 *   счётчик копий) — у каждого списка свой
 */
@Composable
fun DiscCard(
    title: String,
    artist: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cover: Any? = null,
    coverSize: Dp = 64.dp,
    year: String? = null,
    format: String? = null,
    label: String? = null,
    country: String? = null,
    note: String? = null,
    badge: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (cover != null) {
                DiscCover(model = cover, size = coverSize)
                Spacer(Modifier.width(12.dp))
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                DiscDetails(
                    format = format,
                    year = year,
                    label = label,
                    country = country,
                )

                if (note != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                badge?.let {
                    Spacer(Modifier.height(6.dp))
                    it()
                }
            }
        }
    }
}

/**
 * Обложка с состоянием загрузки и заглушкой при ошибке.
 */
@Composable
private fun DiscCover(model: Any, size: Dp) {
    SubcomposeAsyncImage(
        model = model,
        contentDescription = null,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(Modifier.size(size / 3))
            }
        },
        error = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = stringResource(R.string.cd_no_cover),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(size / 2.5f),
                )
            }
        },
    )
}

/**
 * Пары «метка: значение» в том порядке, в котором их принято читать.
 */
@Composable
private fun DiscDetails(
    format: String?,
    year: String?,
    label: String?,
    country: String?,
) {
    val details = listOfNotNull(
        format?.let { stringResource(R.string.disc_label_format) to it },
        year?.let { stringResource(R.string.disc_label_year) to it },
        label?.let { stringResource(R.string.disc_label_label) to it },
        country?.let { stringResource(R.string.disc_label_country) to it },
    )

    if (details.isEmpty()) return

    Spacer(Modifier.height(4.dp))
    details.forEach { (name, value) ->
        MetadataRow(
            label = name,
            value = value,
            labelWidth = null,
            style = MaterialTheme.typography.bodySmall,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            valueColor = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "Поиск — все поля", showBackground = true, widthDp = 380)
@Composable
private fun DiscCardFullPreview() {
    CdVinylCatalogTheme {
        DiscCard(
            title = "Once",
            artist = "Nightwish",
            format = "CD, Album, Limited Edition",
            year = "2004",
            label = "Nuclear Blast",
            country = "Europe",
            onClick = {},
        )
    }
}

@Preview(name = "Коллекция — с заметкой и копиями", showBackground = true, widthDp = 380)
@Composable
private fun DiscCardCollectionPreview() {
    CdVinylCatalogTheme {
        DiscCard(
            title = "Coroner's Sun",
            artist = "Kirlian Camera",
            format = "CD",
            year = "2007",
            note = "подписан группой",
            badge = {
                Text(
                    text = "2 копии",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            onClick = {},
        )
    }
}

@Preview(name = "Минимум полей", showBackground = true, widthDp = 380)
@Composable
private fun DiscCardMinimalPreview() {
    CdVinylCatalogTheme {
        // Только обязательные поля — остальные строки не рисуются
        DiscCard(
            title = "Live",
            artist = "Nightwish",
            onClick = {},
        )
    }
}
