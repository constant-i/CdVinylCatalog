package com.kai.cdvinylcatalog.common.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.catalog.Track
import com.kai.cdvinylcatalog.domain.catalog.galleryImages

/**
 * Блок «информация о релизе»: обложки, заголовок, метаданные, треклист.
 *
 * Один на два экрана деталей — релиз из Discogs и запись коллекции.
 * Всё, что экраны добавляют своего (статус, поле заметки, кнопки, другие копии),
 * передаётся хвостовым слотом [content]: он идёт последним, внутри той же колонки.
 *
 * Стиль общий и не параметризуется: заголовок — `headlineSmall`, заголовки
 * секций — `titleMedium`, метаданные и треклист — в `Card`.
 *
 * Прокрутку и внешние отступы оставляем вызывающему: оба экрана кладут блок
 * в свою `verticalScroll`-колонку со своим `padding`.
 *
 * @param images обложки для [CoverGallery]; по умолчанию все изображения издания,
 *   иначе хотя бы главная
 * @param tracklist треки; на экране коллекции берутся из состояния, а не из [Release]
 * @param isTracklistLoading показать спиннер вместо треклиста, пока грузятся детали
 * @param note строка «Заметка» в метаданных — заметка записи коллекции
 * @param content секции экрана после треклиста
 */
@Composable
fun ReleaseInfo(
    release: Release,
    modifier: Modifier = Modifier,
    images: List<String> = release.galleryImages(),
    tracklist: List<Track> = release.tracklist,
    isTracklistLoading: Boolean = false,
    note: String? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        CoverGallery(images = images, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        ReleaseHeader(
            title = release.title,
            artist = release.artist,
        )
        Spacer(Modifier.height(16.dp))

        ReleaseMetadata(
            year = release.year?.toString(),
            format = release.rawFormat,
            label = release.label,
            country = release.country,
            catalogNumber = release.catalogNumber,
            note = note,
        )

        when {
            isTracklistLoading -> {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            tracklist.isNotEmpty() -> {
                Spacer(Modifier.height(16.dp))
                TracklistSection(tracks = tracklist)
            }
        }

        content()
    }
}

@Preview(name = "Информация о релизе", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun ReleaseInfoPreview() {
    CdVinylCatalogTheme {
        ReleaseInfo(
            release = previewReleaseInfo,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "С заметкой", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun ReleaseInfoNotePreview() {
    CdVinylCatalogTheme {
        ReleaseInfo(
            release = previewReleaseInfo,
            modifier = Modifier.padding(16.dp),
            note = "Фирменный диск",
        )
    }
}

private val previewReleaseInfo = Release(
    id = 1L,
    title = "Once",
    artist = "Nightwish",
    year = 2004,
    barcode = "727361129125",
    coverImageUrl = null,
    label = "Nuclear Blast",
    rawFormat = "CD, Album",
    country = "Europe",
    releaseDate = null,
    catalogNumber = "NB 1291-2",
    notes = null,
    tracklist = listOf(
        Track(
            position = "1",
            title = "Dark Chest Of Wonders",
            duration = "4:28"
        ),
        Track(
            position = "2",
            title = "Wish I Had An Angel",
            duration = "4:03"
        ),
        Track(
            position = "3",
            title = "Nemo",
            duration = "4:36"
        ),
    ),
)
