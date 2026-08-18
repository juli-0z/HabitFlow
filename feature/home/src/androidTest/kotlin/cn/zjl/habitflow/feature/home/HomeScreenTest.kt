package cn.zjl.habitflow.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import cn.zjl.habitflow.designsystem.theme.HabitFlowTheme
import cn.zjl.habitflow.model.Frequency
import cn.zjl.habitflow.model.Habit
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * HomeScreen Compose UI Test（TECH_DESIGN_v1.2 §8.3/§8.4，androidTest）
 *
 * fake VM 直构方案：MockK mock HomeViewModel 并桩 uiState/events 流
 * （依赖 §4.2 Screen 可注入约定，不引入 hilt-android-testing）；
 * onCheckIn/onShowCreateDialog 由测试内联"模拟真实 VM 行为"更新 stateFlow，
 * 从而用 UI 断言验证交互链路（§8.3 示例）。
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun fakeViewModel(
        habits: List<Habit> = emptyList(),
        checkedIds: Set<Long> = emptySet(),
    ): HomeViewModel {
        val viewModel = mockk<HomeViewModel>()
        val stateFlow = MutableStateFlow(
            HomeUiState(habits = habits, isLoading = false, checkedHabitIds = checkedIds),
        )
        every { viewModel.uiState } returns stateFlow
        every { viewModel.events } returns emptyFlow()
        // 模拟真实 VM 行为（§5.2：打卡回推状态 / §2.3：打开编辑器）
        every { viewModel.onCheckIn(any()) } answers {
            stateFlow.value = stateFlow.value.copy(
                checkedHabitIds = stateFlow.value.checkedHabitIds + firstArg<Long>(),
            )
        }
        every { viewModel.onShowCreateDialog() } answers {
            stateFlow.value = stateFlow.value.copy(isEditorVisible = true)
        }
        return viewModel
    }

    private fun setContent(viewModel: HomeViewModel) {
        composeRule.setContent {
            HabitFlowTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }

    @Test
    fun emptyState_showsEmptyView() {
        setContent(fakeViewModel(habits = emptyList()))

        composeRule.onNodeWithText("暂无习惯").assertIsDisplayed()
    }

    @Test
    fun habitList_rendersHabitNames() {
        val habits = listOf(
            Habit(id = 1, name = "晨跑", frequency = Frequency.DAILY, createdAt = 0L),
            Habit(id = 2, name = "阅读", frequency = Frequency.WEEKLY, targetPerWeek = 3, createdAt = 0L),
        )
        setContent(fakeViewModel(habits = habits))

        composeRule.onNodeWithText("晨跑").assertIsDisplayed()
        composeRule.onNodeWithText("阅读").assertIsDisplayed()
    }

    @Test
    fun checkingCheckbox_marksHabitAsChecked() {
        val habits = listOf(Habit(id = 1, name = "晨跑", frequency = Frequency.DAILY, createdAt = 0L))
        setContent(fakeViewModel(habits = habits))

        composeRule.onNodeWithTag("checkin_1").assertIsOff()
        composeRule.onNodeWithTag("checkin_1").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("checkin_1").assertIsOn()
    }

    @Test
    fun fabClick_opensEditorDialog() {
        setContent(fakeViewModel(habits = emptyList()))

        composeRule.onNodeWithContentDescription("新建习惯").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("新建习惯").assertIsDisplayed()   // 弹窗标题
        composeRule.onNodeWithText("名称").assertIsDisplayed()      // 表单字段
    }
}
