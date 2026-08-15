package cn.zjl.habitflow.feature.home

import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.zjl.habitflow.model.Frequency
import cn.zjl.habitflow.model.Habit

/**
 * 习惯列表项（TECH_DESIGN_v1.2 §4.2：Composable 按功能命名，文件同名）
 * 2.4 任务接入打卡 CheckBox 交互。
 */
@Composable
fun HabitListItem(
    habit: Habit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(text = habit.name) },
        supportingContent = { Text(text = frequencyText(habit)) },
        modifier = modifier,
    )
}

private fun frequencyText(habit: Habit): String = when (habit.frequency) {
    Frequency.DAILY -> "每天"
    Frequency.WEEKLY -> "每周 ${habit.targetPerWeek} 次"
}
