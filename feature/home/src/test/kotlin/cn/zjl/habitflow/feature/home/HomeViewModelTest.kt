package cn.zjl.habitflow.feature.home

import cn.zjl.habitflow.data.repository.HabitRepository
import cn.zjl.habitflow.model.Frequency
import cn.zjl.habitflow.model.Habit
import cn.zjl.habitflow.testing.MainDispatcherRule
import cn.zjl.habitflow.testing.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * HomeViewModel 最小状态流转测试（TECH_DESIGN_v1.2 §8.1）
 *
 * 归属说明：完整用例在任务 2.7（MockK + MainDispatcherRule + Turbine 全覆盖）；
 * 本文件为 2.2 的关键状态逻辑最小单测，同时兑现任务 1.10 的"被 2.7 复用"验收
 * （MainDispatcherRule / TestDataFactory 首次被消费）。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<HabitRepository>()

    @Test
    fun `empty habits exposes empty ui state`() = runTest {
        coEvery { repository.observeHabits() } returns flowOf(emptyList<Habit>())

        val viewModel = HomeViewModel(repository)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(emptyList<Habit>(), viewModel.uiState.value.habits)
        assertEquals(null as String?, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `habits from repository are exposed in ui state`() = runTest {
        val habits = listOf(
            TestDataFactory.habit(id = 1, name = "晨跑"),
            TestDataFactory.habit(id = 2, name = "阅读", targetPerWeek = 3, frequency = Frequency.WEEKLY),
        )
        coEvery { repository.observeHabits() } returns flowOf(habits)
        // 2.4：observeHabits 内部会为每个习惯合并 observeChecked 流（§5.2），需 stub
        coEvery { repository.observeChecked(any(), any()) } returns flowOf(false)

        val viewModel = HomeViewModel(repository)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(habits, viewModel.uiState.value.habits)
    }

    @Test
    fun `repository error exposes error message in ui state`() = runTest {
        coEvery { repository.observeHabits() } returns flow<List<cn.zjl.habitflow.model.Habit>> {
            throw IllegalStateException("db broken")
        }

        val viewModel = HomeViewModel(repository)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals("db broken", viewModel.uiState.value.errorMessage)
    }

    // ---- 2.3 编辑器校验（§5.1，归属 2.7 完整用例的提前部分）----

    @Test
    fun `invalid name rejects save without touching repository`() = runTest {
        coEvery { repository.observeHabits() } returns flowOf(emptyList<Habit>())
        val viewModel = HomeViewModel(repository)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        viewModel.onShowCreateDialog()
        viewModel.onSaveHabit(name = "   ", frequency = Frequency.DAILY, targetPerWeek = 0)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isEditorVisible)
        assertEquals("习惯名称不能为空", viewModel.uiState.value.editorErrorMessage)
        coVerify(exactly = 0) { repository.saveHabit(any()) }
    }

    @Test
    fun `valid habit saves and closes editor`() = runTest {
        coEvery { repository.observeHabits() } returns flowOf(emptyList<Habit>())
        coEvery { repository.saveHabit(any()) } returns 1L
        val viewModel = HomeViewModel(repository)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        viewModel.onShowCreateDialog()
        viewModel.onSaveHabit(name = "晨跑", frequency = Frequency.DAILY, targetPerWeek = 0)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isEditorVisible)
        assertEquals(null, viewModel.uiState.value.editorErrorMessage)
        coVerify(exactly = 1) { repository.saveHabit(any()) }
    }
}
