package com.kai.cdvinylcatalog.common.ui.message

import android.content.Context
import com.kai.cdvinylcatalog.common.ui.R
import com.kai.cdvinylcatalog.common.ui.asText

/**
 * Маппинг UI-сообщения в строку для отображения.
 *
 * Доменные ошибки делегирует в AppError.asText() — без дублирования.
 */
fun UiMessage.asText(context: Context): String = when (this) {
    // Доменные ошибки — делегируем
    is UiMessage.Error -> appError.asText(context)

    // Бизнес-сообщения
    UiMessage.ItemNotFound -> context.getString(R.string.msg_item_not_found)
    UiMessage.PhotoImportFailed -> context.getString(R.string.msg_photo_import_failed)

    // UI-нотификации
    UiMessage.RequiredFieldsEmpty -> context.getString(R.string.msg_required_fields)
    is UiMessage.AddedSuccessfully -> context.getString(R.string.msg_added, title)
}