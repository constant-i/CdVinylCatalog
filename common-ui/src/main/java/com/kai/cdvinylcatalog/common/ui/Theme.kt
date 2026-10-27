package com.kai.cdvinylcatalog.common.ui

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Палитра на случай, когда динамический цвет недоступен (Android < 12).
 */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

/**
 * Тема приложения CdVinylCatalog.
 *
 * Приложение намеренно только тёмное: обложки на тёмном фоне читаются лучше,
 * а светлая палитра не проектировалась. Параметра `darkTheme` поэтому нет —
 * если появится переключатель, он вернётся сюда аргументом.
 *
 * Системные панели тоже фиксируются тёмными, в `MainActivity`.
 *
 * @param dynamicColor использовать динамический цвет Material You (Android 12+)
 * @param content контент приложения
 */
@Composable
fun CdVinylCatalogTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
