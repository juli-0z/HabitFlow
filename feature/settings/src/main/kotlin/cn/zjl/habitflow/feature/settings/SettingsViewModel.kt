package cn.zjl.habitflow.feature.settings

import androidx.lifecycle.viewModelScope
import cn.zjl.habitflow.data.datasource.SettingsDataSource
import cn.zjl.habitflow.designsystem.base.BaseViewModel
import cn.zjl.habitflow.designsystem.base.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 设置页 ViewModel（TECH_DESIGN_v1.2 §6.2）
 *
 * - 深色模式：DataStore 持久化（§6.2：SettingsDataSource.isDarkMode Flow）；
 * - onDarkModeChange 乐观更新（UI 立即响应）+ DataStore 持久化（Flow 回推双保险）；
 * - 全局生效：MainActivity 收集本 VM 的 isDarkMode 驱动 HabitFlowTheme(darkTheme)（§11.5 双轨）。
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataSource: SettingsDataSource,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(isLoading = true))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeDarkMode()
    }

    private fun observeDarkMode() {
        viewModelScope.launch {
            settingsDataSource.isDarkMode
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) }
                }
                .collect { isDarkMode ->
                    _uiState.update { it.copy(isDarkMode = isDarkMode, isLoading = false) }
                }
        }
    }

    /** 切换深色模式：乐观更新 + DataStore 持久化（§6.2 setDarkMode） */
    fun onDarkModeChange(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }   // 立即生效
        launchTask(block = {
            settingsDataSource.setDarkMode(enabled)          // 持久化（重启保持）
        })
    }
}

/** 设置页状态（§4.2：页面级 data class，ViewModel 同文件定义） */
data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val isLoading: Boolean = false,          // 初始读取 DataStore 中（M3 3.10 三态）
    val errorMessage: String? = null,        // DataStore 读取失败（M3 3.10 三态）
)
