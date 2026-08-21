package cn.zjl.habitflow.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.zjl.habitflow.designsystem.component.EmptyView
import cn.zjl.habitflow.designsystem.component.ErrorView
import cn.zjl.habitflow.designsystem.component.LoadingView
import cn.zjl.habitflow.model.Habit
import cn.zjl.habitflow.model.StreakStats
import kotlin.math.roundToInt

/**
 * 统计页（M3 3.4，§5.4：近 7/30 天完成率 + 当前/最长连续）
 *
 * - Screen 以 ViewModel 为构造参数（§4.2 约定，禁止内部 hiltViewModel()）；
 * - 三态渲染复用 :core:designsystem 组件（Loading/Empty/Error，3.10 同步受益）；
 * - 每个习惯一张统计卡片：连续（当前/最长）+ 完成率（近 7/30 天）。
 */
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> LoadingView(modifier = modifier)
        uiState.errorMessage != null -> ErrorView(
            modifier = modifier,
            message = uiState.errorMessage.orEmpty(),
        )
        uiState.stats.isEmpty() -> EmptyView(
            modifier = modifier,
            title = "暂无统计数据",
            subtitle = "先去首页创建习惯并打卡吧",
        )
        else -> StatsList(stats = uiState.stats, modifier = modifier)
    }
}

@Composable
private fun StatsList(
    stats: List<HabitStats>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(stats, key = { it.habit.id }) { item ->
            StatCard(habit = item.habit, stats = item.stats)
        }
    }
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
