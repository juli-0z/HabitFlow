package cn.zjl.habitflow.feature.home

import androidx.lifecycle.viewModelScope
import cn.zjl.habitflow.data.repository.HabitRepository
import cn.zjl.habitflow.designsystem.base.BaseViewModel
import cn.zjl.habitflow.designsystem.base.toUserMessage
import cn.zjl.habitflow.domain.HabitValidator
import cn.zjl.habitflow.domain.ValidationResult
import cn.zjl.habitflow.model.Frequency
import cn.zjl.habitflow.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
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

/**
 * 首页 ViewModel（TECH_DESIGN_v1.2 §4.1/§4.3）
 *
 * - 状态：MutableStateFlow 持有，UI 只读；列表 + 三态 + 编辑器状态由 data class 承载；
 * - 意图：UI 调用 onXxx(...)（§4.1）；一次性事件走 BaseViewModel 事件通道。
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeHabits()
    }

    /**
     * 订阅未归档习惯列表（§5.1：软删除过滤在 DAO），
     * 并合并每个习惯当日打卡状态（§5.2：observeChecked Flow），实现 UI 自动刷新。
     */
    private fun observeHabits() {
        viewModelScope.launch {
            habitRepository.observeHabits()
                .flatMapLatest { habits ->
                    observeCheckedStates(habits).map { checkedIds -> habits to checkedIds }
                }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) }
                }
                .collect { (habits, checkedIds) ->
                    _uiState.update {
                        HomeUiState(habits = habits, isLoading = false, checkedHabitIds = checkedIds)
                    }
                }
        }
    }

    /** 当日已打卡习惯 id 集合（combine 合并各习惯的 observeChecked 流） */
    private fun observeCheckedStates(habits: List<Habit>): kotlinx.coroutines.flow.Flow<Set<Long>> {
        if (habits.isEmpty()) return flowOf(emptySet())
        val today = LocalDate.now()
        return combine(habits.map { habit ->
            habitRepository.observeChecked(habit.id, today).map { checked -> if (checked) habit.id else null }
        }) { values -> values.filterNotNull().toSet() }
    }

    // ---- 编辑器意图（2.3，§5.1 新建/编辑共用校验）----

    /** 打开新建弹窗（editingHabit = null 表示新建） */
    fun onShowCreateDialog() {
        _uiState.update { it.copy(isEditorVisible = true, editingHabit = null, editorErrorMessage = null) }
    }

    /** 关闭弹窗（取消） */
    fun onDismissEditor() {
        _uiState.update { it.copy(isEditorVisible = false, editingHabit = null, editorErrorMessage = null) }
    }

    /**
     * 保存习惯：先经 HabitValidator 校验（§5.1 编辑与新建共用），
     * 失败 -> 弹窗内展示错误；成功 -> upsert 并关闭弹窗。
     */
    fun onSaveHabit(name: String, frequency: Frequency, targetPerWeek: Int) {
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
}

/** 首页状态（§4.2：页面级 data class，ViewModel 同文件定义） */
data class HomeUiState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val checkedHabitIds: Set<Long> = emptySet(),     // 当日已打卡习惯（2.4，§5.2）
    val isEditorVisible: Boolean = false,        // 编辑器弹窗可见（2.3）
    val editingHabit: Habit? = null,             // 编辑目标（null = 新建）
    val editorErrorMessage: String? = null,      // 弹窗内校验错误（§5.1）
)

/** 首页一次性事件（§4.2：sealed interface；当前页面暂无发射，预留） */
sealed interface HomeUiEvent {
    data class ShowToast(val message: String) : HomeUiEvent
}
