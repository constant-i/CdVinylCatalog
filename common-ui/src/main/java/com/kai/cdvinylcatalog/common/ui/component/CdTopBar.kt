package com.kai.cdvinylcatalog.common.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.kai.cdvinylcatalog.common.ui.R

/**
 * Топ-бар приложения: заголовок и, если передан [onBack], кнопка «назад».
 *
 * Одна структура на всех экранах вместо восьми копий `TopAppBar` с
 * `navigationIcon` и `IconButton`/`ArrowBack`.
 *
 * Цвета заданы обычными [Color], а не `TopAppBarColors`: тот помечен как
 * экспериментальный, и в публичной сигнатуре заставлял бы каждый экран
 * ставить `@OptIn(ExperimentalMaterial3Api::class)`. `Color.Unspecified`
 * означает «взять из темы».
 *
 * @param onBack действие кнопки «назад»; `null` — кнопки нет (главный экран)
 * @param actions иконки справа; `tint` можно не задавать — берётся из [contentColor]
 * @param containerColor фон бара; например, `Color.Transparent` у сканера
 * @param contentColor цвет заголовка и иконок; например, `Color.White` у сканера
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CdTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
) {
    val themeColors = TopAppBarDefaults.topAppBarColors()
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = containerColor.ifSpecifiedOr(themeColors.containerColor),
        titleContentColor = contentColor.ifSpecifiedOr(themeColors.titleContentColor),
        navigationIconContentColor = contentColor.ifSpecifiedOr(themeColors.navigationIconContentColor),
        actionIconContentColor = contentColor.ifSpecifiedOr(themeColors.actionIconContentColor),
    )

    TopAppBar(
        modifier = modifier,
        title = {
            // Длинные названия релизов не должны ломать высоту бара
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                    )
                }
            }
        },
        actions = actions,
        colors = colors,
    )
}

private fun Color.ifSpecifiedOr(fallback: Color): Color =
    if (isSpecified) this else fallback
