package cn.zjl.habitflow.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.zjl.habitflow.designsystem.component.EmptyView
import cn.zjl.habitflow.designsystem.component.ErrorView
import cn.zjl.habitflow.designsystem.component.LoadingView
import cn.zjl.habitflow.model.Habit

/**
 * 首页（TECH_DESIGN_v1.2 §4.2：Screen 以 ViewModel 为构造参数，
 * 禁止内部调用 hiltViewModel()——VM 在导航装配层获取）。
 *
 * 三态渲染：Loading（加载中）/ Error（加载失败）/ Empty（暂无习惯）/ Content（列表）。
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when {
        uiState.isLoading -> LoadingView(modifier = modifier)
        uiState.errorMessage != null -> ErrorView(
            modifier = modifier,
            message = uiState.errorMessage.orEmpty(),
        )
        uiState.habits.isEmpty() -> EmptyView(
            modifier = modifier,
            title = "暂无习惯",
            subtitle = "点击右下角 + 创建第一个习惯",
        )
        else -> HabitList(habits = uiState.habits, modifier = modifier)
    }
}

@Composable
private fun HabitList(
    habits: List<Habit>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(habits, key = { it.id }) { habit ->
            HabitListItem(habit = habit)
        }
    }
}
