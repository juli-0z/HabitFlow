package cn.zjl.habitflow.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 首页占位（TECH_DESIGN_v1.1 §4.2 Screen 命名约定）
 *
 * 任务 1.12 路由骨架占位；2.2 任务接入 HomeViewModel——
 * Screen 以 ViewModel 为构造参数、VM 在导航装配层获取（§4.2）。
 */
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("首页")
    }
}
