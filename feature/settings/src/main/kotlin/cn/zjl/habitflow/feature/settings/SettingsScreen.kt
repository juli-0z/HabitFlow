package cn.zjl.habitflow.feature.settings

import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 设置页（TECH_DESIGN_v1.2 §4.2：Screen 以 ViewModel 为构造参数，
 * 禁止内部调用 hiltViewModel()——VM 在导航装配层获取）。
 *
 * 深色模式开关：状态来自 DataStore（§6.2），切换经 onDarkModeChange 持久化；
 * 全局主题由 MainActivity 收集本 VM 状态驱动（§11.5 双轨）。
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ListItem(
        headlineContent = { Text(text = "深色模式") },
        supportingContent = { Text(text = "切换立即生效并持久化") },
        trailingContent = {
            Switch(
                checked = uiState.isDarkMode,
                onCheckedChange = viewModel::onDarkModeChange,
            )
        },
        modifier = modifier,
    )
}
