package cn.zjl.habitflow.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
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
        streaks: Map<Long, Int> = emptyMap(),
    ): HomeViewModel {
        val viewModel = mockk<HomeViewModel>()
        val stateFlow =
            MutableStateFlow(
                HomeUiState(
                    habits = habits,
                    isLoading = false,
                    checkedHabitIds = checkedIds,
                    streaks = streaks,
                ),
            )
        every { viewModel.uiState } returns stateFlow
        every { viewModel.events } returns emptyFlow()
        // 模拟真实 VM 行为（§5.2：打卡回推状态 / §2.3：打开编辑器）
        every { viewModel.onCheckIn(any()) } answers {
            stateFlow.value =
                stateFlow.value.copy(
                    checkedHabitIds = stateFlow.value.checkedHabitIds + firstArg<Long>(),
                )
        }
        every { viewModel.onShowCreateDialog() } answers {
            stateFlow.value = stateFlow.value.copy(isEditorVisible = true)
        }
        // M3 3.2：模拟删除链路（长按请求 -> 确认后从列表移除并关闭对话框）
        every { viewModel.onRequestDelete(any()) } answers {
            stateFlow.value = stateFlow.value.copy(habitToDelete = firstArg<Habit>())
        }
        every { viewModel.onConfirmDelete() } answers {
            val target = stateFlow.value.habitToDelete
            stateFlow.value =
                stateFlow.value.copy(
                    habitToDelete = null,
                    habits =
                        if (target == null) {
                            stateFlow.value.habits
                        } else {
                            stateFlow.value.habits.filterNot { it.id == target.id }
                        },
                )
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
        val habits =
            listOf(
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

        composeRule.onNodeWithText("新建习惯").assertIsDisplayed() // 弹窗标题
        composeRule.onNodeWithText("名称").assertIsDisplayed() // 表单字段
    }

    // ---- M3 3.2 删除链路：长按 -> 确认对话框 -> 确认后列表移除 ----

    @Test
    fun longPressHabit_showsDeleteConfirmDialog() {
        val habits = listOf(Habit(id = 1, name = "晨跑", frequency = Frequency.DAILY, createdAt = 0L))
        setContent(fakeViewModel(habits = habits))

        composeRule.onNodeWithText("晨跑").performTouchInput { longClick() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("删除习惯").assertIsDisplayed()
        composeRule.onNodeWithText("取消").assertIsDisplayed()
    }

    @Test
    fun deleteConfirm_removesHabitFromList() {
        val habits = listOf(Habit(id = 1, name = "晨跑", frequency = Frequency.DAILY, createdAt = 0L))
        setContent(fakeViewModel(habits = habits))

        composeRule.onNodeWithText("晨跑").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("删除").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("晨跑").assertDoesNotExist()
    }

    // ---- M3 3.3 今日视图：已完成/未完成分区 + 今日连续展示 ----

    @Test
    fun todayView_showsPendingAndCompletedSections() {
        val habits =
            listOf(
                Habit(id = 1, name = "晨跑", frequency = Frequency.DAILY, createdAt = 0L),
                Habit(id = 2, name = "阅读", frequency = Frequency.DAILY, createdAt = 0L),
            )
        setContent(fakeViewModel(habits = habits, checkedIds = setOf(2L)))

        composeRule.onNodeWithText("待打卡 (1)").assertIsDisplayed()
        composeRule.onNodeWithText("已完成 (1)").assertIsDisplayed()
    }

    @Test
    fun habitItem_showsCurrentStreak() {
        val habits = listOf(Habit(id = 1, name = "晨跑", frequency = Frequency.DAILY, createdAt = 0L))
        setContent(fakeViewModel(habits = habits, streaks = mapOf(1L to 3)))

        composeRule.onNodeWithText("每天 · 连续 3 天").assertIsDisplayed()
    }
}
