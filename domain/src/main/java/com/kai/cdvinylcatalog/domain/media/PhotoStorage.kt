package com.kai.cdvinylcatalog.domain.media

/**
 * Куда сохранять фотографию и как её показать внешнему приложению.
 *
 * @property path абсолютный путь к файлу — хранится в состоянии экрана и в базе
 * @property shareableUri строка вида `content://...`, пригодная для передачи
 *   в камеру и в редактор кропа. Строкой, а не `android.net.Uri`, чтобы
 *   контракт оставался в чистом :domain без Android SDK.
 */
data class PhotoDestination(
    val path: String,
    val shareableUri: String
)

/**
 * Хранилище фотографий пользователя.
 *
 * Абстракция над файловой системой приложения. Presentation-слой не работает
 * с `File`, `FileProvider` и `ContentResolver` напрямую — только через этот
 * интерфейс.
 */
interface PhotoStorage {

    /**
     * Создаёт пустой файл назначения под новую фотографию.
     *
     * Нужен до запуска камеры: `TakePicture` требует готовый `content://`-uri,
     * в который будет записан снимок.
     */
    fun createDestination(): PhotoDestination

    /**
     * Отдаёт `content://`-строку для уже существующего файла [path].
     */
    fun shareableUri(path: String): String

    /**
     * Копирует внешний файл (например, выбранный в галерее) в хранилище приложения.
     *
     * @param sourceUri строка `content://` от системного пикера
     * @return копия в хранилище или `null`, если прочитать источник не удалось
     */
    suspend fun import(sourceUri: String): PhotoDestination?

    /**
     * Проверяет, что файл существует и не пуст.
     *
     * Камера может вернуть `RESULT_OK`, но не записать данные — например,
     * если пользователь отменил съёмку на системном уровне.
     */
    fun isUsable(path: String): Boolean

    /**
     * Удаляет файл, если он есть. Молча игнорирует отсутствие.
     */
    fun delete(path: String)
}
