package com.kai.cdvinylcatalog.feature.add.scan

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Сканер не ходит в сеть: он лишь сообщает навигации штрихкод,
 * а поиск выполняет [com.kai.cdvinylcatalog.feature.add.selection.ReleaseSelectionViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScanViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ScanViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ScanViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `emits NavigateToSelection carrying scanned barcode`() = runTest {
        viewModel.effect.test {
            viewModel.onIntent(ScanContract.Intent.OnBarcodeScanned(BARCODE))

            assertEquals(ScanContract.Effect.NavigateToSelection(BARCODE), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `ignores repeated scans of the same barcode inside debounce window`() = runTest {
        viewModel.effect.test {
            viewModel.onIntent(ScanContract.Intent.OnBarcodeScanned(BARCODE))
            viewModel.onIntent(ScanContract.Intent.OnBarcodeScanned(BARCODE))
            viewModel.onIntent(ScanContract.Intent.OnBarcodeScanned(BARCODE))

            assertEquals(ScanContract.Effect.NavigateToSelection(BARCODE), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `accepts a different barcode without waiting for the debounce window`() = runTest {
        viewModel.effect.test {
            viewModel.onIntent(ScanContract.Intent.OnBarcodeScanned(BARCODE))
            viewModel.onIntent(ScanContract.Intent.OnBarcodeScanned(OTHER_BARCODE))

            assertEquals(ScanContract.Effect.NavigateToSelection(BARCODE), awaitItem())
            assertEquals(ScanContract.Effect.NavigateToSelection(OTHER_BARCODE), awaitItem())
            expectNoEvents()
        }
    }

    private companion object {
        const val BARCODE = "602547378781"
        const val OTHER_BARCODE = "5099750153821"
    }
}
