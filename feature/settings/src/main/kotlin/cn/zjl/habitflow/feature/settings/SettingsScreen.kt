package cn.zjl.habitflow.feature.settings

import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.zjl.habitflow.designsystem.component.ErrorView
import cn.zjl.habitflow.designsystem.component.LoadingView

/**
 * 设置页（TECH_DESIGN_v1.2 §4.2：Screen 以 ViewModel 为构造参数，
 * 禁止内部调用 hiltViewModel()——VM 在导航装配层获取）。
 *
 * - 深色模式开关：状态来自 DataStore（§6.2），切换经 onDarkModeChange 持久化；
 * - 全局主题由 MainActivity 收集本 VM 状态驱动（§11.5 双轨）；
 * - 三态（M3 3.10）：DataStore 读取中 Loading / 读取失败 Error / 正常内容。
 *   设置页为静态列表，无数据驱动的空态，Empty 不适用（验收口径"断库不白屏"由 Error 兜底）。
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> LoadingView(modifier = modifier)
        uiState.errorMessage != null -> ErrorView(
            modifier = modifier,
            message = uiState.errorMessage.orEmpty(),
        )
        else -> SettingsContent(
            isDarkMode = uiState.isDarkMode,
            onDarkModeChange = viewModel::onDarkModeChange,
            modifier = modifier,
        )
    }
}

@Composable
private fun SettingsContent(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(text = "深色模式") },
        supportingContent = { Text(text = "切换立即生效并持久化") },
        trailingContent = {
            Switch(
                checked = isDarkMode,
                onCheckedChange = onDarkModeChange,
            )
        },
        modifier = modifier,
    )
}
