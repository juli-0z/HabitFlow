package cn.zjl.habitflow.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                checkedHabitIds = uiState.checkedHabitIds,
                streaks = uiState.streaks,
                onCheckIn = viewModel::onCheckIn,
                onCheckOut = viewModel::onCheckOut,
                onEdit = viewModel::onShowEditDialog,
                onRequestDelete = viewModel::onRequestDelete,
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

    // M3 3.2：删除确认对话框（长按列表项后弹出，确认后软删除 archiveHabit）
    uiState.habitToDelete?.let { habit ->
        DeleteConfirmDialog(
            habitName = habit.name,
            onConfirm = viewModel::onConfirmDelete,
            onDismiss = viewModel::onDismissDelete,
        )
    }
}

@Composable
private fun HabitList(
    habits: List<Habit>,
    checkedHabitIds: Set<Long>,
    streaks: Map<Long, Int>,
    onCheckIn: (Long) -> Unit,
    onCheckOut: (Long) -> Unit,
    onEdit: (Habit) -> Unit,
    onRequestDelete: (Habit) -> Unit,
    modifier: Modifier = Modifier,
) {
    // M3 3.3：已完成/未完成分区（checkedHabitIds 为唯一事实来源，分区与打卡状态严格一致）
    val pending = habits.filter { it.id !in checkedHabitIds }
    val completed = habits.filter { it.id in checkedHabitIds }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        if (pending.isNotEmpty()) {
            item(key = "section_pending") { SectionHeader(text = "待打卡 (${pending.size})") }
            items(pending, key = { it.id }) { habit ->
                HabitListItem(
                    habit = habit,
                    isChecked = false,
                    currentStreak = streaks[habit.id] ?: 0,
                    onCheckedChange = { checked ->
                        if (checked) onCheckIn(habit.id) else onCheckOut(habit.id)
                    },
                    onEdit = onEdit,
                    onRequestDelete = onRequestDelete,
                )
            }
        }
        if (completed.isNotEmpty()) {
            item(key = "section_completed") { SectionHeader(text = "已完成 (${completed.size})") }
            items(completed, key = { it.id }) { habit ->
                HabitListItem(
                    habit = habit,
                    isChecked = true,
                    currentStreak = streaks[habit.id] ?: 0,
                    onCheckedChange = { checked ->
                        if (checked) onCheckIn(habit.id) else onCheckOut(habit.id)
                    },
                    onEdit = onEdit,
                    onRequestDelete = onRequestDelete,
                )
            }
        }
    }
}

/** 分区标题（M3 3.3）：待打卡 / 已完成，含计数 */
@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

/** 删除确认对话框（M3 3.2）：软删除不可逆（统计保留），需用户二次确认 */
@Composable
private fun DeleteConfirmDialog(
    habitName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "删除习惯") },
        text = { Text(text = "确定删除「$habitName」吗？历史打卡记录会保留用于统计。") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
