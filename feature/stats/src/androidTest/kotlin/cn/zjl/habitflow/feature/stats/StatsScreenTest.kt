package cn.zjl.habitflow.feature.stats

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import cn.zjl.habitflow.designsystem.theme.HabitFlowTheme
import cn.zjl.habitflow.model.Frequency
import cn.zjl.habitflow.model.Habit
import cn.zjl.habitflow.model.StreakStats
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * StatsScreen Compose UI Test（M3 3.4/3.5，androidTest）
 *
 * fake VM 直构方案（§4.2 Screen 可注入约定，§8.3）：mock StatsViewModel 并桩 uiState/events。
 */
@RunWith(AndroidJUnit4::class)
class StatsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun fakeViewModel(
        stats: List<HabitStats> = emptyList(),
        heatmap: List<HeatmapCell> = emptyList(),
    ): StatsViewModel {
        val viewModel = mockk<StatsViewModel>()
        every { viewModel.uiState } returns MutableStateFlow(
            StatsUiState(stats = stats, heatmap = heatmap, isLoading = false),
        )
        every { viewModel.events } returns emptyFlow()
        return viewModel
    }

    private fun setContent(viewModel: StatsViewModel) {
        composeRule.setContent {
            HabitFlowTheme {
                StatsScreen(viewModel = viewModel)
            }
        }
    }

    @Test
    fun statsList_rendersHabitStats() {
        val item = HabitStats(
            habit = Habit(id = 1, name = "晨跑", frequency = Frequency.DAILY, createdAt = 0L),
            stats = StreakStats(
                currentStreak = 3,
                longestStreak = 5,
                sevenDayCompletionRate = 3.0 / 7.0,
                thirtyDayCompletionRate = 0.5,
            ),
        )
        setContent(fakeViewModel(stats = listOf(item)))

        composeRule.onNodeWithText("晨跑").assertIsDisplayed()
        composeRule.onNodeWithText("3 天").assertIsDisplayed()     // 当前连续
        composeRule.onNodeWithText("5 天").assertIsDisplayed()     // 最长连续
        composeRule.onNodeWithText("43%").assertIsDisplayed()      // 3/7 ≈ 42.86 → 43%
        composeRule.onNodeWithText("50%").assertIsDisplayed()      // 0.5 → 50%
    }

    @Test
    fun emptyStats_showsEmptyView() {
        setContent(fakeViewModel(stats = emptyList()))

        composeRule.onNodeWithText("暂无统计数据").assertIsDisplayed()
    }

    @Test
    fun heatmap_sectionShowsWhenDataExists() {
        val item = HabitStats(
            habit = Habit(id = 1, name = "晨跑", frequency = Frequency.DAILY, createdAt = 0L),
            stats = StreakStats(currentStreak = 1, longestStreak = 1),
        )
        setContent(
            fakeViewModel(
                stats = listOf(item),
                heatmap = listOf(HeatmapCell(date = LocalDate.now(), completedCount = 2)),
            ),
        )

        composeRule.onNodeWithText("打卡热力图").assertIsDisplayed()
        composeRule.onNodeWithTag("habit_heatmap").assertIsDisplayed()
    }
}
