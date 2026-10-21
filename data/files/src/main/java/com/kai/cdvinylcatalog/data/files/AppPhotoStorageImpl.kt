package com.kai.cdvinylcatalog.data.files

import android.content.Context
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.kai.cdvinylcatalog.domain.media.PhotoDestination
import com.kai.cdvinylcatalog.domain.media.PhotoStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Хранилище фотографий в приватной директории приложения.
 *
 * Файлы лежат в `filesDir/photos`. Отдаются наружу через `FileProvider`,
 * объявленный в манифесте `:app` с authority `${applicationId}.fileprovider`.
 */
@Singleton
class AppPhotoStorageImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PhotoStorage {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun createDestination(): PhotoDestination = destinationFor(newFile())

    override fun shareableUri(path: String): String =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}$FILE_PROVIDER_SUFFIX",
            File(path)
        ).toString()

    override suspend fun import(sourceUri: String): PhotoDestination? =
        withContext(ioDispatcher) {
            val destination = newFile()
            try {
                val input = context.contentResolver.openInputStream(sourceUri.toUri())
                    ?: return@withContext null
                input.use { source ->
                    destination.outputStream().use { target ->
                        source.copyTo(target)
                    }
                }
                destinationFor(destination)
            } catch (_: IOException) {
                // Источник недоступен или прервался — считаем импорт неудачным
                destination.delete()
                null
            } catch (_: SecurityException) {
                // Провайдер не выдал доступ к content://
                destination.delete()
                null
            }
        }

    override fun isUsable(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.length() > 0
    }

    override fun delete(path: String) {
        File(path).delete()
    }

    private fun newFile(): File {
        val dir = File(context.filesDir, PHOTOS_SUB_DIR).apply { mkdirs() }
        return File(dir, "${UUID.randomUUID()}.$PHOTO_EXTENSION")
    }

    private fun destinationFor(file: File) = PhotoDestination(
        path = file.absolutePath,
        shareableUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}$FILE_PROVIDER_SUFFIX",
            file
        ).toString()
    )

    private companion object {
        const val PHOTOS_SUB_DIR = "photos"
        const val PHOTO_EXTENSION = "jpg"
        const val FILE_PROVIDER_SUFFIX = ".fileprovider"
    }
}
