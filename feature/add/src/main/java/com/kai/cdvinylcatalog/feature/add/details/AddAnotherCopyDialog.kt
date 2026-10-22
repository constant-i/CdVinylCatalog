package com.kai.cdvinylcatalog.feature.add.details

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.feature.add.R

/**
 * Спрашивает, добавить ли ещё одну копию уже имеющегося диска.
 *
 * Discogs не запрещает иметь несколько экземпляров одного издания,
 * поэтому это не ошибка, а уточнение.
 */
@Composable
internal fun AddAnotherCopyDialog(
    releaseTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.details_already_in_collection_title)) },
        text = {
            Text(
                stringResource(
                    R.string.details_already_in_collection_message,
                    releaseTitle
                )
            )
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

@Preview(name = "Уже в коллекции", showBackground = true, locale = "ru")
@Composable
private fun AddAnotherCopyDialogPreview() {
    CdVinylCatalogTheme {
        AddAnotherCopyDialog(
            releaseTitle = "Once",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
