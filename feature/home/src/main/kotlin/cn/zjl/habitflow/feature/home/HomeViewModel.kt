package cn.zjl.habitflow.feature.home

import androidx.lifecycle.viewModelScope
import cn.zjl.habitflow.data.repository.HabitRepository
import cn.zjl.habitflow.designsystem.base.BaseViewModel
import cn.zjl.habitflow.designsystem.base.toUserMessage
import cn.zjl.habitflow.domain.HabitValidator
import cn.zjl.habitflow.domain.StreakCalculator
import cn.zjl.habitflow.domain.ValidationResult
import cn.zjl.habitflow.model.Frequency
import cn.zjl.habitflow.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 首页 ViewModel（TECH_DESIGN_v1.2 §4.1/§4.3）
 *
 * - 状态：MutableStateFlow 持有，UI 只读；列表 + 三态 + 编辑器状态由 data class 承载；
 * - 意图：UI 调用 onXxx(...)（§4.1）；一次性事件走 BaseViewModel 事件通道。
 */
@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
    ) : BaseViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        init {
            observeHabits()
        }

        /**
         * 订阅未归档习惯列表（§5.1：软删除过滤在 DAO），
         * 并合并每个习惯的今日状态（是否打卡 + 今日连续，M3 3.3），实现 UI 自动刷新。
         */
        private fun observeHabits() {
            viewModelScope.launch {
                habitRepository
                    .observeHabits()
                    .flatMapLatest { habits ->
                        observeTodayStates(habits).map { todayStates -> habits to todayStates }
                    }.catch { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) }
                    }.collect { (habits, todayStates) ->
                        _uiState.update {
                            it.copy(
                                habits = habits,
                                isLoading = false,
                                checkedHabitIds =
                                    todayStates
                                        .filterValues { state -> state.isCheckedToday }
                                        .keys,
                                streaks = todayStates.mapValues { state -> state.value.currentStreak },
                            )
                        }
                    }
            }
        }

        /**
         * 合并各习惯的今日状态：observeChecked（是否打卡，§5.2）+
         * observeCompletionStats（近 N 天完成情况，§5.4）经 StreakCalculator 计算今日连续（M3 3.3）。
         * 连续计算窗口见 [STREAK_WINDOW_DAYS]（MVP 足够，更长连续由记录窗口截断）。
         */
        private fun observeTodayStates(habits: List<Habit>): kotlinx.coroutines.flow.Flow<Map<Long, TodayState>> {
            if (habits.isEmpty()) return flowOf(emptyMap())
            val today = LocalDate.now()
            return combine(
                habits.map { habit ->
                    combine(
                        habitRepository.observeChecked(habit.id, today),
                        habitRepository.observeCompletionStats(habit.id, STREAK_WINDOW_DAYS),
                    ) { checked, stats ->
                        val records = stats.toMap()
                        habit.id to
                            TodayState(
                                isCheckedToday = checked,
                                currentStreak = StreakCalculator.currentStreak(records, today),
                            )
                    }
                },
            ) { states -> states.toMap() }
        }

        // ---- 编辑器意图（2.3，§5.1 新建/编辑共用校验）----

        /** 打开新建弹窗（editingHabit = null 表示新建） */
        fun onShowCreateDialog() {
            _uiState.update { it.copy(isEditorVisible = true, editingHabit = null, editorErrorMessage = null) }
        }

        /** 打开编辑弹窗（M3 3.1：editingHabit 非空表示编辑，复用同一表单） */
        fun onShowEditDialog(habit: Habit) {
            _uiState.update { it.copy(isEditorVisible = true, editingHabit = habit, editorErrorMessage = null) }
        }

        /** 关闭弹窗（取消） */
        fun onDismissEditor() {
            _uiState.update { it.copy(isEditorVisible = false, editingHabit = null, editorErrorMessage = null) }
        }

        /**
         * 保存习惯：先经 HabitValidator 校验（§5.1 编辑与新建共用），
         * 失败 -> 弹窗内展示错误；成功 -> upsert 并关闭弹窗。
         * [iconRes]/[colorHex] 带默认值（M3 3.1 新增），旧调用保持兼容（不破坏既有单测）。
         */
        fun onSaveHabit(
            name: String,
            frequency: Frequency,
            targetPerWeek: Int,
            iconRes: String? = null,
            colorHex: String = "",
        ) {
            when (val result = HabitValidator.validate(name, frequency, targetPerWeek)) {
                is ValidationResult.Success -> {
                    val editing = _uiState.value.editingHabit
                    // 注：launchTask 有两个函数类型参数，trailing lambda 会绑定最后一个（onError），
                    // 必须显式命名 block（§4.4 文档示例的隐含陷阱，交付说明已标注）
                    launchTask(block = {
                        habitRepository.saveHabit(
                            Habit(
                                id = editing?.id ?: 0L,
                                name = name.trim(),
                                frequency = frequency,
                                targetPerWeek = targetPerWeek,
                                iconRes = iconRes,
                                colorHex = colorHex,
                                createdAt = editing?.createdAt ?: System.currentTimeMillis(),
                            ),
                        )
                        _uiState.update {
                            it.copy(isEditorVisible = false, editingHabit = null, editorErrorMessage = null)
                        }
                    })
                }
                is ValidationResult.Failure -> {
                    _uiState.update { it.copy(editorErrorMessage = result.message) }
                }
            }
        }
        // ---- 打卡意图（2.4，§5.2：打卡 = 插入当日记录，撤销 = 删除当日记录）----

        fun onCheckIn(habitId: Long) {
            launchTask(block = {
                habitRepository.checkIn(habitId, LocalDate.now())
            })
        }

        fun onCheckOut(habitId: Long) {
            launchTask(block = {
                habitRepository.checkOut(habitId, LocalDate.now())
            })
        }

        // ---- 删除意图（M3 3.2：长按 -> 确认对话框 -> 软删除 isArchived=1）----

        /** 请求删除：仅记录待删除目标，由 UI 弹出确认对话框（不直接删） */
        fun onRequestDelete(habit: Habit) {
            _uiState.update { it.copy(habitToDelete = habit) }
        }

        /** 确认删除：软删除（archiveHabit），observeActiveHabits 自动过滤使其从列表消失；记录保留用于统计 */
        fun onConfirmDelete() {
            val target = _uiState.value.habitToDelete ?: return
            launchTask(block = {
                habitRepository.archiveHabit(target.id)
                _uiState.update { it.copy(habitToDelete = null) }
            }, onError = {
                _uiState.update { it.copy(habitToDelete = null) }
            })
        }

        /** 取消删除：清除待删除目标，关闭确认对话框 */
        fun onDismissDelete() {
            _uiState.update { it.copy(habitToDelete = null) }
        }

        private companion object {
            /** 今日连续计算窗口（M3 3.3）：覆盖 MVP 连续场景，更长连续由窗口截断（文档化取舍） */
            const val STREAK_WINDOW_DAYS = 60
        }
    }

/** 首页状态（§4.2：页面级 data class，ViewModel 同文件定义） */
data class HomeUiState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val checkedHabitIds: Set<Long> = emptySet(), // 当日已打卡习惯（2.4，§5.2）
    val streaks: Map<Long, Int> = emptyMap(), // habitId -> 今日连续（M3 3.3，>0 才展示）
    val isEditorVisible: Boolean = false, // 编辑器弹窗可见（2.3）
    val editingHabit: Habit? = null, // 编辑目标（null = 新建）
    val editorErrorMessage: String? = null, // 弹窗内校验错误（§5.1）
    val habitToDelete: Habit? = null, // 待删除目标（M3 3.2，弹确认框用）
)

/** 习惯今日状态（M3 3.3：是否已打卡 + 今日连续天数；excused 豁免日语义见 §5.3，3.6 后补） */
data class TodayState(
    val isCheckedToday: Boolean = false,
    val currentStreak: Int = 0,
)

/** 首页一次性事件（§4.2：sealed interface；当前页面暂无发射，预留） */
sealed interface HomeUiEvent {
    data class ShowToast(
        val message: String,
    ) : HomeUiEvent
}
