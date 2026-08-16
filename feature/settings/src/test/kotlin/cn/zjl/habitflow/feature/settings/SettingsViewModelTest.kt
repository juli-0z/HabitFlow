package cn.zjl.habitflow.feature.settings

import cn.zjl.habitflow.data.datasource.SettingsDataSource
import cn.zjl.habitflow.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Test
    fun `dark mode state follows data store`() = runTest {
        every { dataSource.isDarkMode } returns flowOf(true)

        val viewModel = SettingsViewModel(dataSource)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isDarkMode)
    }

    @Test
    fun `toggle updates state and persists via setDarkMode`() = runTest {
        every { dataSource.isDarkMode } returns flowOf(false)
        coEvery { dataSource.setDarkMode(any()) } returns Unit

        val viewModel = SettingsViewModel(dataSource)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        viewModel.onDarkModeChange(true)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isDarkMode)
        coVerify(exactly = 1) { dataSource.setDarkMode(true) }
    }
}
