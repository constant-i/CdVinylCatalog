package com.kai.cdvinylcatalog.domain.usecase.collection

import com.kai.cdvinylcatalog.domain.collection.CollectionItem
import com.kai.cdvinylcatalog.domain.repository.CollectionReader
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

/**
 * Офлайн-поиск диска в коллекции по штрихкоду.
 *
 * Нужен, когда сети нет: Discogs недоступен, и единственный способ ответить
 * пользователю — локальная база.
 *
 * Возвращает **список**, потому что один штрихкод может соответствовать
 * нескольким изданиям. Поэтому результат нельзя трактовать как
 * «этот релиз уже есть» — только как «диск с таким штрихкодом уже есть».
 */
class FindItemsByBarcodeUseCase @Inject constructor(
    private val reader: CollectionReader
) {
    suspend operator fun invoke(barcode: String): Outcome<List<CollectionItem>> =
        reader.findByBarcode(barcode)
}
