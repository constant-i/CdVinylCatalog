package com.kai.cdvinylcatalog.data.network

import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import com.kai.cdvinylcatalog.data.network.dto.PaginationDto
import com.kai.cdvinylcatalog.data.network.dto.ReleaseDetailsDto
import com.kai.cdvinylcatalog.data.network.dto.SearchResponseDto
import com.kai.cdvinylcatalog.data.network.dto.SearchResultDto
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.result.AppErrorException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Discogs `database/search` возвращает один и тот же релиз несколько раз.
 * Дубликаты ломают `LazyColumn` (ключ = `release.id`), поэтому источник
 * обязан фильтровать уже отданные релизы.
 */
class DiscogsSearchPagingSourceTest {

    @Test
    fun `refresh drops duplicated ids`() = runTest {
        val api = FakeDiscogsApi(
            pages = mapOf(1 to listOf(result(1), result(2), result(2), result(3))),
            totalPages = 2
        )
        val source = DiscogsSearchPagingSource(api, query = "Nightwish")

        val page = source.load(refresh(loadSize = 50)).asPage()

        assertEquals(listOf(1L, 2L, 3L), page.data.map { it.id })
        assertEquals(2, page.nextKey)
        assertNull(page.prevKey)
    }

    @Test
    fun `append skips items already delivered on previous page`() = runTest {
        val api = FakeDiscogsApi(
            pages = mapOf(
                1 to listOf(result(1), result(2)),
                2 to listOf(result(2), result(3))
            ),
            totalPages = 2
        )
        val source = DiscogsSearchPagingSource(api, query = "Nightwish")

        source.load(refresh(loadSize = 50)).asPage()
        val appended = source.load(append(key = 2, loadSize = 50)).asPage()

        assertEquals(listOf(3L), appended.data.map { it.id })
        assertNull(appended.nextKey)
        assertEquals(1, appended.prevKey)
    }

    @Test
    fun `page made entirely of duplicates continues to next page`() = runTest {
        val api = FakeDiscogsApi(
            pages = mapOf(
                1 to listOf(result(1), result(2)),
                2 to listOf(result(1), result(2)),
                3 to listOf(result(3))
            ),
            totalPages = 3
        )
        val source = DiscogsSearchPagingSource(api, query = "Nightwish")

        source.load(refresh(loadSize = 50)).asPage()
        val appended = source.load(append(key = 2, loadSize = 50)).asPage()

        assertEquals(listOf(3L), appended.data.map { it.id })
        assertNull(appended.nextKey)
    }

    @Test
    fun `stops pagination on incomplete page when metadata is missing`() = runTest {
        val api = FakeDiscogsApi(
            pages = mapOf(1 to listOf(result(1), result(2))),
            totalPages = null
        )
        val source = DiscogsSearchPagingSource(api, query = "Nightwish")

        val page = source.load(refresh(loadSize = 50)).asPage()

        assertEquals(listOf(1L, 2L), page.data.map { it.id })
        assertNull(page.nextKey)
    }

    @Test
    fun `maps network failure into domain error`() = runTest {
        val api = FailingDiscogsApi()
        val source = DiscogsSearchPagingSource(api, query = "Nightwish")

        val result = source.load(refresh(loadSize = 50))

        assertTrue(result is LoadResult.Error)
        assertTrue((result as LoadResult.Error).throwable is AppErrorException)
    }

    private fun refresh(loadSize: Int): LoadParams<Int> =
        LoadParams.Refresh(key = null, loadSize = loadSize, placeholdersEnabled = false)

    private fun append(key: Int, loadSize: Int): LoadParams<Int> =
        LoadParams.Append(key = key, loadSize = loadSize, placeholdersEnabled = false)

    @Suppress("UNCHECKED_CAST")
    private fun LoadResult<Int, Release>.asPage() =
        this as LoadResult.Page<Int, Release>

    private fun result(id: Long) = SearchResultDto(
        id = id,
        title = "Release $id",
        year = "2004",
        country = "EU",
        format = listOf("CD"),
        label = listOf("Nuclear Blast"),
        coverImage = null,
        barcode = null
    )

    private class FakeDiscogsApi(
        private val pages: Map<Int, List<SearchResultDto>>,
        private val totalPages: Int?
    ) : DiscogsApi {

        override suspend fun searchByBarcode(
            barcode: String,
            type: String,
            page: Int,
            perPage: Int,
            token: String
        ): SearchResponseDto = response(page)

        override suspend fun searchByText(
            query: String,
            type: String,
            page: Int,
            perPage: Int,
            token: String
        ): SearchResponseDto = response(page)

        override suspend fun getReleaseDetails(
            releaseId: Long,
            token: String
        ): ReleaseDetailsDto = error("not used")

        private fun response(page: Int): SearchResponseDto {
            val results = pages[page].orEmpty()
            return SearchResponseDto(
                results = results,
                pagination = totalPages?.let { total ->
                    PaginationDto(
                        page = page,
                        pages = total,
                        perPage = results.size,
                        items = results.size
                    )
                }
            )
        }
    }

    private class FailingDiscogsApi : DiscogsApi {

        override suspend fun searchByBarcode(
            barcode: String,
            type: String,
            page: Int,
            perPage: Int,
            token: String
        ): SearchResponseDto = throw IOException()

        override suspend fun searchByText(
            query: String,
            type: String,
            page: Int,
            perPage: Int,
            token: String
        ): SearchResponseDto = throw IOException()

        override suspend fun getReleaseDetails(
            releaseId: Long,
            token: String
        ): ReleaseDetailsDto = error("not used")
    }
}
