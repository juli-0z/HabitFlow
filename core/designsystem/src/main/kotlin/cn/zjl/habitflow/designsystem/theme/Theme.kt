package cn.zjl.habitflow.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * 应用主题（TECH_DESIGN_v1.1 §3.1/§11.5）
 *
 * - 双轨主题：Manifest 用 XML 主题（Theme.HabitFlow）启动，Compose 内由本主题接管；
 * - [darkTheme] 由调用方传入（:feature:settings 深色开关 2.6 任务接入 DataStore 值，
 *   未接入前默认跟随系统）。
 */
@Composable
fun HabitFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = HabitFlowTypography,
        shapes = HabitFlowShapes,
        content = content,
    )
}
