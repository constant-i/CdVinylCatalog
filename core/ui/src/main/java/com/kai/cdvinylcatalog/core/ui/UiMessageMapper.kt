package com.kai.cdvinylcatalog.core.ui

import android.content.Context
import com.kai.cdvinylcatalog.core.model.UiMessage

/**
 * Маппинг доменного сообщения в строку.
 *
 * Обычная функция (не Composable), принимает Context.
 * Используется в Route для Toast через context.getString.
 */
fun UiMessage.asText(context: Context): String = when (this) {
    UiMessage.ReleaseNotFound -> context.getString(R.string.error_not_found)
    UiMessage.SearchEmpty -> context.getString(R.string.error_search_empty)
    UiMessage.NoInternet -> context.getString(R.string.error_no_internet)
    UiMessage.Timeout -> context.getString(R.string.error_timeout)
    UiMessage.Unauthorized -> context.getString(R.string.error_unauthorized)
    UiMessage.RateLimited -> context.getString(R.string.error_rate_limited)
    is UiMessage.ServerError -> context.getString(R.string.error_server, code)
    UiMessage.InvalidBarcode -> context.getString(R.string.error_invalid_barcode)
    UiMessage.ItemNotFound -> context.getString(R.string.error_item_not_found)
    UiMessage.RequiredFieldsEmpty -> context.getString(R.string.error_required_fields)
    is UiMessage.AddedSuccessfully -> context.getString(R.string.success_added, title)
    is UiMessage.SaveError -> context.getString(R.string.error_save, message ?: "")
    is UiMessage.Unknown -> context.getString(R.string.error_unknown, message ?: "")
}