package com.kai.cdvinylcatalog.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kai.cdvinylcatalog.R
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.common.ui.component.CdTopBar

/**
 * Главный экран приложения.
 * Показывает четыре действия: сканирование, поиск, ручное добавление, коллекция.
 *
 * Карточки делят высоту экрана поровну (`weight`), поэтому на экране
 * с малой высотой они сжимаются сами и все четыре остаются видны,
 * без прокрутки и без обрезки.
 */
@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToManualAdd: () -> Unit,
    onNavigateToCollection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CdTopBar(title = stringResource(R.string.home_title))
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            // Отступ фиксированный, а не spacedBy: иначе на низком экране
            // он съедал бы место, которое нужно самим карточкам
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeActionCard(
                icon = Icons.Default.QrCodeScanner,
                title = stringResource(R.string.home_scan_title),
                subtitle = stringResource(R.string.home_scan_subtitle),
                onClick = onNavigateToScan,
                isPrimary = true,
                modifier = Modifier.weight(1f)
            )
            HomeActionCard(
                icon = Icons.Default.Search,
                title = stringResource(R.string.home_search_title),
                subtitle = stringResource(R.string.home_search_subtitle),
                onClick = onNavigateToSearch,
                modifier = Modifier.weight(1f)
            )
            HomeActionCard(
                icon = Icons.Default.Edit,
                title = stringResource(R.string.home_manual_title),
                subtitle = stringResource(R.string.home_manual_subtitle),
                onClick = onNavigateToManualAdd,
                modifier = Modifier.weight(1f)
            )
            HomeActionCard(
                icon = Icons.Default.LibraryMusic,
                title = stringResource(R.string.home_collection_title),
                subtitle = stringResource(R.string.home_collection_subtitle),
                onClick = onNavigateToCollection,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Карточка действия на главном экране.
 * При нажатии — пружинная анимация масштаба.
 *
 * Цвета берутся парой контейнер/на-контейнере ([CardDefaults.cardColors]):
 * `primaryContainer`/`onPrimaryContainer` для основного действия и
 * `surfaceVariant`/`onSurfaceVariant` для остальных. `contentColor` дальше
 * подхватывают `Icon` и `Text` через `LocalContentColor`, поэтому явные `tint`
 * и `color` не нужны.
 *
 * Иерархия заголовка и подписи — только типографикой (`titleMedium` против
 * `bodySmall`). Полупрозрачность не используем: третьего уровня приглушения
 * в схеме нет, а на динамических цветах `alpha` не гарантирует контраст.
 *
 * @param isPrimary выделить ли карточку как основное действие
 */
@Composable
private fun HomeActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Физика движения: пружина
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    val colorScheme = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        interactionSource = interactionSource,
        colors = CardDefaults.cardColors(
            containerColor =
                if (isPrimary) colorScheme.primaryContainer
                else colorScheme.surfaceVariant,
            contentColor =
                if (isPrimary) colorScheme.onPrimaryContainer
                else colorScheme.onSurfaceVariant
        )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isCompact = maxHeight < COMPACT_CARD_HEIGHT
            if (isCompact) {
                CompactContent(icon, title)
            } else {
                FullContent(icon, title, subtitle)
            }
        }
    }
}

/** Обычная раскладка: иконка сверху, заголовок и подпись под ней. */
@Composable
private fun FullContent(icon: ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Малая высота: иконка и заголовок в одну строку, подпись скрыта.
 */
@Composable
private fun CompactContent(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * С какой высоты карточка переключается с полной раскладки на компактную.
 *
 * Полная требует около 123dp: отступы 20+20, иконка 32, отступы 12+4,
 * заголовок ~19, подпись ~16. Если места меньше, полная раскладка
 * не помещается вообще.
 */
private val COMPACT_CARD_HEIGHT = 130.dp

@Preview(name = "Высокий экран", showBackground = true, heightDp = 800)
@Composable
private fun HomeScreenPreview() {
    CdVinylCatalogTheme {
        HomeScreen(
            onNavigateToScan = {},
            onNavigateToSearch = {},
            onNavigateToManualAdd = {},
            onNavigateToCollection = {}
        )
    }
}

/**
 * Малая высота — альбомная ориентация или разделённый экран.
 * Проверяет, что карточки сжимаются и все четыре остаются видны.
 */
@Preview(name = "Низкий экран", showBackground = true, heightDp = 420)
@Composable
private fun HomeScreenShortPreview() {
    CdVinylCatalogTheme {
        HomeScreen(
            onNavigateToScan = {},
            onNavigateToSearch = {},
            onNavigateToManualAdd = {},
            onNavigateToCollection = {}
        )
    }
}
