package com.kai.cdvinylcatalog.core.model

/**
 * Доменные сообщения для отображения в UI.
 *
 * ViewModel отправляет тип сообщения, а UI мапит его в строку
 * через stringResource — так ViewModel не знает про Context и ресурсы.
 *
 * Это принцип разделения ответственности: ViewModel отвечает за логику,
 * UI — за отображение. Тестируется легко — проверяем тип, а не строку.
 */
sealed interface UiMessage {

    /** Релиз не найден в Discogs */
    data object ReleaseNotFound : UiMessage

    /** Ничего не найдено */
    data object SearchEmpty : UiMessage

    /** Нет подключения к интернету */
    data object NoInternet : UiMessage

    /** Сервер не отвечает */
    data object Timeout : UiMessage

    /** Проблема с токеном Discogs */
    data object Unauthorized : UiMessage

    /** Слишком много запросов */
    data object RateLimited : UiMessage

    /** Ошибка сервера с кодом */
    data class ServerError(val code: Int) : UiMessage

    /** Некорректный штрихкод */
    data object InvalidBarcode : UiMessage

    /** Запись не найдена */
    data object ItemNotFound : UiMessage

    /** Обязательные поля не заполнены */
    data object RequiredFieldsEmpty : UiMessage

    /** Запись успешно добавлена (с названием) */
    data class AddedSuccessfully(val title: String) : UiMessage

    /** Ошибка сохранения */
    data class SaveError(val message: String?) : UiMessage

    /** Неизвестная ошибка с текстом */
    data class Unknown(val message: String?) : UiMessage
}