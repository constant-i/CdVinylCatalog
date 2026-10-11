package com.kai.cdvinylcatalog.feature.add.scan

import app.cash.turbine.test
import com.kai.cdvinylcatalog.core.model.Release
import com.kai.cdvinylcatalog.feature.add.domain.SearchReleaseUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ScanViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var searchRelease: SearchReleaseUseCase
    private lateinit var viewModel: ScanViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchRelease = mockk()
        viewModel = ScanViewModel(searchRelease)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `emits NavigateToSelection on successful scan`() = runTest {
        val releases = listOf(createRelease())
        coEvery { searchRelease(any()) } returns Result.success(releases)

        viewModel.effect.test {
            viewModel.onIntent(ScanContract.Intent.OnBarcodeScanned("602547378781"))
            advanceTimeBy(100.milliseconds)

            val effect = awaitItem()
            assertTrue(effect is ScanContract.Effect.NavigateToSelection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits ShowToast when release not found`() = runTest {
        coEvery { searchRelease(any()) } returns Result.success(emptyList())

        viewModel.effect.test {
            viewModel.onIntent(ScanContract.Intent.OnBarcodeScanned("602547378781"))
            advanceTimeBy(100.milliseconds)

            val effect = awaitItem()
            assertTrue(effect is ScanContract.Effect.ShowToast)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `debounce prevents duplicate scans within 2 seconds`() = runTest {
        coEvery { searchRelease(any()) } returns Result.success(emptyList())

        viewModel.onIntent(ScanContract.Intent.OnBarcodeScanned("602547378781"))
        viewModel.onIntent(ScanContract.Intent.OnBarcodeScanned("602547378781"))
        advanceTimeBy(100.milliseconds)

        // Второй вызов должен быть проигнорирован
        io.mockk.coVerify(exactly = 1) { searchRelease(any()) }
    }

    private fun createRelease() = Release(
        id = 1, title = "Encore", artist = "Sarah Brightman",
        year = 2007, barcode = null, coverImageUrl = null, label = null,
        rawFormat = null, country = null, releaseDate = null,
        catalogNumber = null, notes = null
    )
}