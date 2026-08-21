package cn.zjl.habitflow.feature.stats

import cn.zjl.habitflow.data.repository.HabitRepository
import cn.zjl.habitflow.model.Frequency
import cn.zjl.habitflow.model.Habit
import cn.zjl.habitflow.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * StatsViewModel 统计计算测试（M3 3.4，§5.4）
 *
 * 验收口径"与手工计算结果一致"：用已知数据集断言 7/30 天完成率与当前/最长连续，
 * 数据可人工按 §5.3 连续规则与完成率口径复算核对。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<HabitRepository>()

    private fun habit(id: Long = 1, name: String = "晨跑") = Habit(
        id = id,
        name = name,
        frequency = Frequency.DAILY,
        createdAt = 0L,
    )

    /** 构造 60 天窗口：offset 0 = 59 天前（最早），offset 59 = 今天；trueOffsets 为已打卡的 offset */
    private fun completionWindow(trueOffsets: Set<Int>): List<Pair<LocalDate, Boolean>> {
        val today = LocalDate.now()
        return (0 until 60).map { offset ->
            today.minusDays(59L - offset) to (offset in trueOffsets)
        }
    }

    @Test
    fun `empty habits exposes empty stats`() = runTest {
        coEvery { repository.observeHabits() } returns flowOf(emptyList<Habit>())

        val viewModel = StatsViewModel(repository)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(emptyList<HabitStats>(), viewModel.uiState.value.stats)
        assertEquals(emptyList<HeatmapCell>(), viewModel.uiState.value.heatmap)
        assertEquals(null as String?, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `stats match manual calculation on known data`() = runTest {
        // 已知数据设计（可人工复算）：
        // - 当前连续 3：offset 57/58/59 打卡，56 未打卡（§5.3：今天已打从今天起算）；
        // - 最长连续 5：offset 20..24（位于 30 天窗口之外，验证最长连续用 60 天全窗口）；
        // - 近 7 天（offset 53..59）：命中 {57,58,59} = 3/7；
        // - 近 30 天（offset 30..59）：命中 {40,41,42,57,58,59} = 6/30。
        val trueOffsets = setOf(20, 21, 22, 23, 24, 40, 41, 42, 57, 58, 59)
        coEvery { repository.observeHabits() } returns flowOf(listOf(habit(id = 1)))
        coEvery { repository.observeCompletionStats(1L, any()) } returns flowOf(completionWindow(trueOffsets))

        val viewModel = StatsViewModel(repository)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val item = viewModel.uiState.value.stats.single()
        assertEquals(1L, item.habit.id)
        assertEquals(3, item.stats.currentStreak)
        assertEquals(5, item.stats.longestStreak)
        assertEquals(3.0 / 7.0, item.stats.sevenDayCompletionRate, 1e-9)
        assertEquals(6.0 / 30.0, item.stats.thirtyDayCompletionRate, 1e-9)
    }

    @Test
    fun `repository error exposes error message`() = runTest {
        coEvery { repository.observeHabits() } returns flow<List<Habit>> {
            throw IllegalStateException("db broken")
        }

        val viewModel = StatsViewModel(repository)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals("db broken", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `heatmap aggregates completion counts across habits`() = runTest {
        val today = LocalDate.now()
        coEvery { repository.observeHabits() } returns flowOf(listOf(habit(id = 1), habit(id = 2)))
        // A：昨天+今天打卡；B：仅今天打卡 -> 今天完成数 2，昨天完成数 1
        coEvery { repository.observeCompletionStats(1L, any()) } returns flowOf(completionWindow(setOf(58, 59)))
        coEvery { repository.observeCompletionStats(2L, any()) } returns flowOf(completionWindow(setOf(59)))

        val viewModel = StatsViewModel(repository)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val heatmap = viewModel.uiState.value.heatmap
        assertEquals(60, heatmap.size)                       // 与 WINDOW_DAYS 一致
        assertEquals(today, heatmap.last().date)             // 最后一格 = 今天
        assertEquals(2, heatmap.last().completedCount)       // 两个习惯都完成今天
        assertEquals(1, heatmap[heatmap.size - 2].completedCount)   // 昨天只有 A
        assertEquals(0, heatmap.first().completedCount)      // 最早一天无打卡
    }
}
