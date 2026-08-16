package cn.zjl.habitflow.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
 * 三态渲染：Loading / Error / Empty / Content（列表）；FAB 打开新建弹窗（§5.1）。
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onShowCreateDialog) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "新建习惯")
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingView(modifier = Modifier.padding(innerPadding))
            uiState.errorMessage != null -> ErrorView(
                modifier = Modifier.padding(innerPadding),
                message = uiState.errorMessage.orEmpty(),
            )
            uiState.habits.isEmpty() -> EmptyView(
                modifier = Modifier.padding(innerPadding),
                title = "暂无习惯",
                subtitle = "点击右下角 + 创建第一个习惯",
            )
            else -> HabitList(
                habits = uiState.habits,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

    if (uiState.isEditorVisible) {
        HabitEditorDialog(
            initialHabit = uiState.editingHabit,
            errorMessage = uiState.editorErrorMessage,
            onSave = viewModel::onSaveHabit,
            onDismiss = viewModel::onDismissEditor,
        )
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
