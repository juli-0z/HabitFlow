package cn.zjl.habitflow.feature.settings

import cn.zjl.habitflow.data.datasource.SettingsDataSource
import cn.zjl.habitflow.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * SettingsViewModel 最小状态测试（TECH_DESIGN_v1.2 §6.2/§8.1）
 *
 * 覆盖：DataStore 值初始化同步、切换持久化（setDarkMode 调用）。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dataSource = mockk<SettingsDataSource>()
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)

    @Test
    fun `dark mode state follows data store`() =
        runTest {
            every { dataSource.isDarkMode } returns flowOf(true)
            every { dataSource.isReminderEnabled } returns flowOf(false)

            val viewModel = SettingsViewModel(dataSource, reminderScheduler)
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

            assertEquals(true, viewModel.uiState.value.isDarkMode)
        }

    @Test
    fun `toggle updates state and persists via setDarkMode`() =
        runTest {
            every { dataSource.isDarkMode } returns flowOf(false)
            every { dataSource.isReminderEnabled } returns flowOf(false)
            coEvery { dataSource.setDarkMode(any()) } returns Unit

            val viewModel = SettingsViewModel(dataSource, reminderScheduler)
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

            viewModel.onDarkModeChange(true)
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

            assertEquals(true, viewModel.uiState.value.isDarkMode)
            coVerify(exactly = 1) { dataSource.setDarkMode(true) }
        }

    // ---- M3 3.10 三态：Loading 过渡 / 错误兜底 ----

    @Test
    fun `loading resolves after data store emission`() =
        runTest {
            every { dataSource.isDarkMode } returns flowOf(false)
            every { dataSource.isReminderEnabled } returns flowOf(false)

            val viewModel = SettingsViewModel(dataSource, reminderScheduler)

            assertEquals(true, viewModel.uiState.value.isLoading) // 初始 Loading（3.10）
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

            assertEquals(false, viewModel.uiState.value.isLoading)
            assertEquals(null as String?, viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `data store error exposes error message`() =
        runTest {
            every { dataSource.isDarkMode } returns
                flow {
                    throw IllegalStateException("datastore broken")
                }
            every { dataSource.isReminderEnabled } returns flowOf(false)

            val viewModel = SettingsViewModel(dataSource, reminderScheduler)
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

            assertEquals(false, viewModel.uiState.value.isLoading)
            assertEquals("datastore broken", viewModel.uiState.value.errorMessage)
        }

    // ---- M3 3.8 每日提醒开关（§5：持久化 + WorkManager 调度/取消）----

    @Test
    fun `toggle reminder persists and schedules`() =
        runTest {
            every { dataSource.isDarkMode } returns flowOf(false)
            every { dataSource.isReminderEnabled } returns flowOf(false)
            coEvery { dataSource.setReminderEnabled(any()) } returns Unit

            val viewModel = SettingsViewModel(dataSource, reminderScheduler)
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

            viewModel.onToggleReminder(true)
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

            assertEquals(true, viewModel.uiState.value.isReminderEnabled)
            coVerify(exactly = 1) { dataSource.setReminderEnabled(true) }
            verify(exactly = 1) { reminderScheduler.enable() }
        }

    @Test
    fun `disable reminder cancels schedule`() =
        runTest {
            every { dataSource.isDarkMode } returns flowOf(false)
            every { dataSource.isReminderEnabled } returns flowOf(true)
            coEvery { dataSource.setReminderEnabled(any()) } returns Unit

            val viewModel = SettingsViewModel(dataSource, reminderScheduler)
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

            viewModel.onToggleReminder(false)
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

            assertEquals(false, viewModel.uiState.value.isReminderEnabled)
            coVerify(exactly = 1) { dataSource.setReminderEnabled(false) }
            verify(exactly = 1) { reminderScheduler.disable() }
        }
}
