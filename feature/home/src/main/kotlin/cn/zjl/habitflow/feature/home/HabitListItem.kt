package cn.zjl.habitflow.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import cn.zjl.habitflow.model.Frequency
import cn.zjl.habitflow.model.Habit

/**
 * 习惯列表项（TECH_DESIGN_v1.2 §4.2：Composable 按功能命名，文件同名）
 *
 * - 前导：习惯图标（按 colorHex 着色，未知回退主题 primary，M3 3.1）；
 * - 尾部：打卡 Checkbox（2.4，勾选=打卡/取消=撤销，testTag 定位约定不变）；
 * - 点击列表项 = 打开编辑（M3 3.1）；长按列表项 = 请求删除（M3 3.2）；
 *   点击/长按 Checkbox 不会触发——Checkbox 消费自身点击；
 * - 状态来自当日打卡记录（§5.2：记录表存在与否表示打卡状态）。
 */
@Composable
fun HabitListItem(
    habit: Habit,
    isChecked: Boolean,
    currentStreak: Int = 0,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: (Habit) -> Unit,
    onRequestDelete: (Habit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconTint = colorFromString(habit.colorHex) ?: MaterialTheme.colorScheme.primary

    ListItem(
        headlineContent = { Text(text = habit.name) },
        supportingContent = { Text(text = listItemSubtitle(habit, currentStreak)) },
        leadingContent = {
            Icon(
                imageVector = iconFromString(habit.iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)) // 图标着色圆底（M3 3.1）
                        .testTag("habit_icon_${habit.id}"),
            )
        },
        trailingContent = {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag("checkin_${habit.id}"), // §8.3 UI 测试定位约定
            )
        },
        modifier =
            modifier.combinedClickable(
                onClick = { onEdit(habit) }, // M3 3.1：点击项打开编辑
                onLongClick = { onRequestDelete(habit) }, // M3 3.2：长按项请求删除
            ),
    )
}

/** 副标题：频率 + 今日连续（M3 3.3，连续 >0 才展示） */
private fun listItemSubtitle(
    habit: Habit,
    currentStreak: Int,
): String {
    val frequency =
        when (habit.frequency) {
            Frequency.DAILY -> "每天"
            Frequency.WEEKLY -> "每周 ${habit.targetPerWeek} 次"
        }
    return if (currentStreak > 0) "$frequency · 连续 $currentStreak 天" else frequency
}
