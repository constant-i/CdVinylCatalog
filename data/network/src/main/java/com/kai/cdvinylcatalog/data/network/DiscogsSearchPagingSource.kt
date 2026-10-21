package com.kai.cdvinylcatalog.data.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kai.cdvinylcatalog.data.network.dto.SearchResponseDto
import com.kai.cdvinylcatalog.data.network.dto.toDomain
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.result.AppErrorException
import kotlinx.coroutines.CancellationException

/**
 * Постраничная загрузка результатов поиска из Discogs.
 *
 * Ошибки сети маппятся в доменную [com.kai.cdvinylcatalog.domain.result.AppError]
 * и передаются в UI через [AppErrorException] — Presentation-слой не знает
 * о Retrofit и прочих сетевых исключениях.
 *
 * ### Дедупликация
 * `database/search` в Discogs умеет возвращать один и тот же релиз несколько раз
 * (например, если он совпал и по исполнителю, и по названию). Дубликаты ломают
 * `LazyColumn`, у которого ключ — `release.id`. Поэтому источник фильтрует уже
 * отданные релизы по ID.
 *
 * Так как `PagingSource.load()` никогда не вызывается конкурентно, набор
 * просмотренных ID можно держать обычным `MutableSet`.
 */
class DiscogsSearchPagingSource(
    private val api: DiscogsApi,
    private val query: String? = null,
    private val barcode: String? = null,
) : PagingSource<Int, Release>() {

    /** ID релизов, уже отданных в UI в рамках текущей загрузки. */
    private val seenIds = mutableSetOf<Long>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Release> {
        // Новая загрузка с нуля — начинаем дедупликацию заново
        if (params is LoadParams.Refresh) seenIds.clear()

        var page = params.key ?: FIRST_PAGE
        val prevKey = if (page == FIRST_PAGE) null else page - 1
        val items = mutableListOf<Release>()

        return try {
            repeat(MAX_PAGES_PER_LOAD) {
                val response = request(page, params.loadSize)

                response.results.forEach { dto ->
                    val release = dto.toDomain()
                    if (seenIds.add(release.id)) items += release
                }

                if (isLastPage(page, response, params.loadSize)) {
                    return LoadResult.Page(items, prevKey, nextKey = null)
                }

                // Данных достаточно — отдаём страницу, следующая начнётся со page + 1
                if (items.isNotEmpty()) {
                    return LoadResult.Page(items, prevKey, nextKey = page + 1)
                }

                // Страница целиком состояла из дубликатов — идём дальше
                page += 1
            }

            // Защита от зацикливания: отдаём то, что набрали
            LoadResult.Page(items, prevKey, nextKey = if (items.isEmpty()) null else page + 1)
        } catch (e: CancellationException) {
            // Отмена — не ошибка загрузки: пробрасываем, чтобы Paging не показывал «Ошибка»
            throw e
        } catch (e: Exception) {
            LoadResult.Error(AppErrorException(mapExceptionToAppError(e)))
        }
    }

    private suspend fun request(page: Int, loadSize: Int): SearchResponseDto =
        if (barcode != null) {
            api.searchByBarcode(
                barcode = barcode,
                page = page,
                perPage = loadSize,
                token = NetworkConstants.DISCOGS_TOKEN
            )
        } else {
            api.searchByText(
                query = query.orEmpty(),
                page = page,
                perPage = loadSize,
                token = NetworkConstants.DISCOGS_TOKEN
            )
        }

    private fun isLastPage(
        page: Int,
        response: SearchResponseDto,
        loadSize: Int
    ): Boolean {
        val totalPages = response.pagination?.pages
        return if (totalPages != null) {
            page >= totalPages
        } else {
            // Нет метаданных — считаем концом неполную страницу
            response.results.size < loadSize
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Release>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

    private companion object {
        const val FIRST_PAGE = 1
        const val MAX_PAGES_PER_LOAD = 5
    }
}
