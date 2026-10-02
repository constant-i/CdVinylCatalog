package com.kai.cdvinylcatalog.core.model

/**
 * Результат поиска релиза по штрихкоду.
 */
//sealed class ScanResult {
//    /** Релиз найден и его нет в коллекции */
//    data class Found(val release: Release) : ScanResult()
//
//    /** Релиз не найден в базе Discogs */
//    data class NotFound(val barcode: String) : ScanResult()
//
//    /** Ошибка сети или API */
//    data class Error(val error: DiscogsError) : ScanResult()
//}