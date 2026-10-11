package com.kai.cdvinylcatalog.feature.add.domain

import com.kai.cdvinylcatalog.core.model.Release
import com.kai.cdvinylcatalog.core.network.DiscogsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchReleaseUseCaseTest {

    private lateinit var repository: DiscogsRepository
    private lateinit var useCase: SearchReleaseUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SearchReleaseUseCase(repository)
    }

    @Test
    fun `returns failure when barcode is blank`() = runTest {
        val result = useCase("")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.searchByBarcode(any()) }
    }

    @Test
    fun `returns failure when barcode is too short`() = runTest {
        val result = useCase("123")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.searchByBarcode(any()) }
    }

    @Test
    fun `calls repository with trimmed barcode`() = runTest {
        val barcode = "  602547378781  "
        val expected = Result.success(emptyList<Release>())
        coEvery { repository.searchByBarcode("602547378781") } returns expected

        val result = useCase(barcode)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.searchByBarcode("602547378781") }
    }

    @Test
    fun `returns repository result on success`() = runTest {
        val releases = listOf(createRelease())
        coEvery { repository.searchByBarcode(any()) } returns Result.success(releases)

        val result = useCase("602547378781")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.size == 1)
    }

    private fun createRelease() = Release(
        id = 1, title = "Encore", artist = "Sarah Brightman",
        year = 2007, barcode = null, coverImageUrl = null, label = null,
        rawFormat = null, country = null, releaseDate = null,
        catalogNumber = null, notes = null
    )
}