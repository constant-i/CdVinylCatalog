package com.kai.cdvinylcatalog.domain.usecase.collection

import com.kai.cdvinylcatalog.domain.collection.CollectionItem
import com.kai.cdvinylcatalog.domain.repository.CollectionReader
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

/**
 * Читает одну запись коллекции по локальному ID.
 */
class GetCollectionItemUseCase @Inject constructor(
    private val reader: CollectionReader
) {
    suspend operator fun invoke(id: Long): Outcome<CollectionItem?> = reader.getItemById(id)
}
