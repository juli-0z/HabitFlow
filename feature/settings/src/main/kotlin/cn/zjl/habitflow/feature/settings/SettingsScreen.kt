package cn.zjl.habitflow.feature.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.zjl.habitflow.designsystem.component.ErrorView
import cn.zjl.habitflow.designsystem.component.LoadingView

/**
 * 设置页（TECH_DESIGN_v1.3 §4.2：Screen 以 ViewModel 为构造参数，
 * 禁止内部调用 hiltViewModel()——VM 在导航装配层获取）。
 *
 * - 深色模式开关：状态来自 DataStore（§6.2），切换经 onDarkModeChange 持久化；
 * - 每日提醒开关（M3 3.8）：持久化 + WorkManager 调度；开启时请求 POST_NOTIFICATIONS（API 33+），
 *   未授权时降级为无通知模式（Worker 内静默跳过，§5 风险预案）；
 * - 三态（M3 3.10）：DataStore 读取中 Loading / 读取失败 Error / 正常内容。
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> LoadingView(modifier = modifier)
        uiState.errorMessage != null ->
            ErrorView(
                modifier = modifier,
                message = uiState.errorMessage.orEmpty(),
            )
        else ->
            SettingsContent(
                isDarkMode = uiState.isDarkMode,
                isReminderEnabled = uiState.isReminderEnabled,
                onDarkModeChange = viewModel::onDarkModeChange,
                onToggleReminder = viewModel::onToggleReminder,
                modifier = modifier,
            )
    }
}

@Composable
private fun SettingsContent(
    isDarkMode: Boolean,
    isReminderEnabled: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    // POST_NOTIFICATIONS 请求回调：无论授权与否都开启开关（拒绝则降级为无通知模式，§5 评估约定）
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            onToggleReminder(true)
        }

    Column(modifier = modifier) {
        ListItem(
            headlineContent = { Text(text = "深色模式") },
            supportingContent = { Text(text = "切换立即生效并持久化") },
            trailingContent = {
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onDarkModeChange,
                )
            },
        )
        ListItem(
            headlineContent = { Text(text = "每日打卡提醒") },
            supportingContent = { Text(text = "每日 20:00 提醒打卡（M3 3.8）") },
            trailingContent = {
                Switch(
                    checked = isReminderEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (needsPostNotificationPermission(context)) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onToggleReminder(true)
                            }
                        } else {
                            onToggleReminder(false)
                        }
                    },
                )
            },
        )
    }
}

/** 是否需要 POST_NOTIFICATIONS 权限（API 33+ 且未授予，M3 3.8） */
private fun needsPostNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
