package com.kai.cdvinylcatalog.feature.add.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.feature.add.R

/**
 * Выбор формата носителя перед добавлением в коллекцию.
 *
 * Показывается, когда Discogs не отдал распознаваемый формат.
 */
@Composable
internal fun FormatPickerDialog(
    selectedFormat: Format,
    onFormatSelected: (Format) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.details_choose_format)) },
        text = {
            Column {
                Format.entries
                    .filter { it != Format.UNKNOWN }
                    .forEach { format ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedFormat == format,
                                    onClick = { onFormatSelected(format) }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFormat == format,
                                onClick = { onFormatSelected(format) }
                            )
                            Text(text = format.name)
                        }
                    }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.common_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Preview(name = "Выбор формата", showBackground = true, locale = "ru")
@Composable
private fun FormatPickerDialogPreview() {
    CdVinylCatalogTheme {
        FormatPickerDialog(
            selectedFormat = Format.CD,
            onFormatSelected = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}
