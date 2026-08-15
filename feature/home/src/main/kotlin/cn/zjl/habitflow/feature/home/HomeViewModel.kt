package cn.zjl.habitflow.feature.home

import androidx.lifecycle.viewModelScope
import cn.zjl.habitflow.data.repository.HabitRepository
import cn.zjl.habitflow.designsystem.base.BaseViewModel
import cn.zjl.habitflow.designsystem.base.toUserMessage
import cn.zjl.habitflow.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 首页 ViewModel（TECH_DESIGN_v1.2 §4.1/§4.3）
 *
 * - 状态：MutableStateFlow 持有，UI 只读；列表 + 三态（加载/空/错误）由 data class 承载；
 * - 意图：UI 调用 onXxx(...)；事件：一次性事件走 BaseViewModel 事件通道（当前页面暂无）。
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

    /** 订阅未归档习惯列表（§5.1：软删除过滤在 DAO），Flow 响应式自动刷新 */
    private fun observeHabits() {
        viewModelScope.launch {
            habitRepository.observeHabits()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) }
                }
                .collect { habits ->
                    _uiState.update { HomeUiState(habits = habits, isLoading = false) }
                }
        }
    }
}

/** 首页状态（§4.2：页面级 data class，ViewModel 同文件定义） */
data class HomeUiState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/** 首页一次性事件（§4.2：sealed interface；当前页面暂无发射，预留） */
sealed interface HomeUiEvent {
    data class ShowToast(val message: String) : HomeUiEvent
}
