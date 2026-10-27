package com.kai.cdvinylcatalog.common.ui.message

import com.kai.cdvinylcatalog.domain.result.AppError

/**
 * UI-сообщения — то, что показывается пользователю.
 *
 * Три категории:
 * - Доменные ошибки — оборачиваются через [Error], не дублируются.
 * - Бизнес-сообщения — результат операции не про ошибку.
 * - UI-нотификации — обратная связь по действиям пользователя.
 *
 * Не содержит Android-зависимостей. Маппинг в строку — в :common-ui.
 */
sealed interface UiMessage {

    // ─── Доменные ошибки — обёртка над AppError ───
    data class Error(val appError: AppError) : UiMessage

    // ─── Бизнес-сообщения ───
    data object ItemNotFound : UiMessage
    data object PhotoImportFailed : UiMessage

    // ─── UI-нотификации ───
    data object RequiredFieldsEmpty : UiMessage
    data class AddedSuccessfully(val title: String) : UiMessage
}