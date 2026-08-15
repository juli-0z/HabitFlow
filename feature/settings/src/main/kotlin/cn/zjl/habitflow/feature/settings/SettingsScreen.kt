package cn.zjl.habitflow.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 设置页占位（TECH_DESIGN_v1.1 §4.2）
 *
 * 任务 1.12 路由骨架占位；2.6 任务接入深色模式开关（DataStore，§6.2）。
 */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("设置")
    }
}
