package cn.zjl.habitflow.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.zjl.habitflow.designsystem.component.EmptyView
import cn.zjl.habitflow.designsystem.component.ErrorView
import cn.zjl.habitflow.designsystem.component.LoadingView
import cn.zjl.habitflow.model.Habit
import cn.zjl.habitflow.model.StreakStats
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * 统计页（M3 3.4/3.5，§5.4：近 7/30 天完成率 + 当前/最长连续 + 周粒度热力图）
 *
 * - Screen 以 ViewModel 为构造参数（§4.2 约定，禁止内部 hiltViewModel()）；
 * - 三态渲染复用 :core:designsystem 组件（Loading/Empty/Error，3.10 同步受益）；
 * - 热力图（3.5）：Compose Canvas 手绘，列=周、行=周一~周日，颜色按"当日完成习惯数"分 5 级。
 */
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> LoadingView(modifier = modifier)
        uiState.errorMessage != null ->
            ErrorView(
                modifier = modifier,
                message = uiState.errorMessage.orEmpty(),
            )
        uiState.stats.isEmpty() ->
            EmptyView(
                modifier = modifier,
                title = "暂无统计数据",
                subtitle = "先去首页创建习惯并打卡吧",
            )
        else ->
            StatsList(
                stats = uiState.stats,
                heatmap = uiState.heatmap,
                modifier = modifier,
            )
    }
}

@Composable
private fun StatsList(
    stats: List<HabitStats>,
    heatmap: List<HeatmapCell>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // M3 3.5：热力图区（周粒度颜色分级，置顶）
        if (heatmap.isNotEmpty()) {
            item(key = "heatmap") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "打卡热力图",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    HeatmapView(cells = heatmap)
                }
            }
        }
        items(stats, key = { it.habit.id }) { item ->
            StatCard(habit = item.habit, stats = item.stats)
        }
    }
}

/**
 * 热力图（M3 3.5，Compose Canvas 手绘）：
 * 列 = 周（周一为一周起点），7 行 = 周一~周日；颜色按当日完成习惯数分 5 级（见 [heatmapLevelColor]）。
 */
@Composable
private fun HeatmapView(
    cells: List<HeatmapCell>,
    modifier: Modifier = Modifier,
) {
    if (cells.isEmpty()) return
    val colorScheme = MaterialTheme.colorScheme
    val firstDate = cells.first().date
    val lastDate = cells.last().date
    // 对齐到周一：起始周一 = 首日前最近的周一（含当天）
    val startMonday = firstDate.minusDays((firstDate.dayOfWeek.value - 1).toLong())
    val spanDays = ChronoUnit.DAYS.between(startMonday, lastDate) + 1
    val cellSize = 14.dp
    val gap = 3.dp
    val cellsByDate = cells.associateBy { it.date }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(
            modifier =
                modifier
                    .fillMaxWidth()
                    .height((cellSize + gap) * 7 - gap)
                    .testTag("habit_heatmap"),
        ) {
            val cellPx = cellSize.toPx()
            val gapPx = gap.toPx()
            val primary = colorScheme.primary
            val surface = colorScheme.surfaceVariant
            for (offset in 0 until spanDays) {
                val date = startMonday.plusDays(offset)
                val row = date.dayOfWeek.value - 1
                val col = (offset / 7).toInt()
                val count = cellsByDate[date]?.completedCount ?: 0
                drawRect(
                    color = heatmapLevelColor(count, primary, surface),
                    topLeft = Offset(col * (cellPx + gapPx), row * (cellPx + gapPx)),
                    size = Size(cellPx, cellPx),
                )
            }
        }
        // 图例（低 -> 高）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "少",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
            )
            (0..4).forEach { level ->
                Box(
                    modifier =
                        Modifier
                            .size(12.dp)
                            .background(heatmapLevelColor(level, colorScheme.primary, colorScheme.surfaceVariant)),
                )
            }
            Text(
                text = "多",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** 颜色分级：0=灰，1/2/3/≥4 为主题绿渐深（M3 3.5） */
private fun heatmapLevelColor(
    count: Int,
    primary: Color,
    surface: Color,
): Color =
    when {
        count <= 0 -> surface
        count == 1 -> primary.copy(alpha = 0.25f)
        count == 2 -> primary.copy(alpha = 0.50f)
        count == 3 -> primary.copy(alpha = 0.75f)
        else -> primary
    }

@Composable
private fun StatCard(
    habit: Habit,
    stats: StreakStats,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                StatValue(label = "当前连续", value = "${stats.currentStreak} 天")
                StatValue(label = "最长连续", value = "${stats.longestStreak} 天")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                StatValue(label = "近7天", value = percent(stats.sevenDayCompletionRate))
                StatValue(label = "近30天", value = percent(stats.thirtyDayCompletionRate))
            }
        }
    }
}

@Composable
private fun StatValue(
    label: String,
    value: String,
) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun percent(rate: Double): String = "${(rate * 100).roundToInt()}%"
