package cn.zjl.habitflow.feature.stats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 统计页占位（TECH_DESIGN_v1.1 §4.2）
 *
 * 任务 1.12 路由骨架占位；3.4 任务接入 StatsViewModel 与统计数据。
 */
@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("统计")
    }
}
