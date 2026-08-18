package cn.zjl.habitflow.feature.home

import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import cn.zjl.habitflow.model.Frequency
import cn.zjl.habitflow.model.Habit

/**
 * 习惯列表项（TECH_DESIGN_v1.2 §4.2：Composable 按功能命名，文件同名）
 *
 * 2.4：CheckBox 接入打卡交互——勾选 = 打卡（onCheckedChange(true)），
 * 取消 = 撤销打卡；状态来自当日打卡记录（§5.2：记录表存在与否表示打卡状态）。
 */
@Composable
fun HabitListItem(
    habit: Habit,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(text = habit.name) },
        supportingContent = { Text(text = frequencyText(habit)) },
        leadingContent = {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag("checkin_${habit.id}"),   // §8.3 UI 测试定位约定
            )
        },
        modifier = modifier,
    )
}

private fun frequencyText(habit: Habit): String = when (habit.frequency) {
    Frequency.DAILY -> "每天"
    Frequency.WEEKLY -> "每周 ${habit.targetPerWeek} 次"
}
