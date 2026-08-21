package cn.zjl.habitflow.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cn.zjl.habitflow.model.Frequency
import cn.zjl.habitflow.model.Habit

/**
 * 新建/编辑习惯弹窗（TECH_DESIGN_v1.2 §5.1；M3 3.1 补全图标/颜色选择）
 *
 * - 新建与编辑共用同一表单与校验（HabitValidator 在 ViewModel 层调用，§5.1）；
 * - 字段：名称、频率（每日/每周 N 次）、目标次数、图标、颜色；
 * - iconRes/colorHex 存字符串（非资源 id，避免混淆漂移，§6.1）；
 * - 空名称时保存按钮禁用（任务 2.3 验收）；其余校验失败由 ViewModel 回传 [errorMessage]；
 * - 表单状态用 rememberSaveable（配置变更不丢输入，§4.5 状态恢复约定）。
 */
@Composable
fun HabitEditorDialog(
    initialHabit: Habit?,
    errorMessage: String?,
    onSave: (name: String, frequency: Frequency, targetPerWeek: Int, iconRes: String?, colorHex: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf(initialHabit?.name.orEmpty()) }
    var frequency by rememberSaveable { mutableStateOf(initialHabit?.frequency ?: Frequency.DAILY) }
    var targetPerWeek by rememberSaveable { mutableIntStateOf(initialHabit?.targetPerWeek?.coerceAtLeast(1) ?: 1) }
    var iconRes by rememberSaveable { mutableStateOf(initialHabit?.iconRes ?: HABIT_ICON_OPTIONS.first().key) }
    var colorHex by rememberSaveable { mutableStateOf(initialHabit?.colorHex ?: HABIT_COLOR_OPTIONS.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(text = if (initialHabit == null) "新建习惯" else "编辑习惯") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    placeholder = { Text("如：每天跑步 30 分钟") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Frequency.entries.forEach { item ->
                        FilterChip(
                            selected = frequency == item,
                            onClick = { frequency = item },
                            label = { Text(if (item == Frequency.DAILY) "每天" else "每周") },
                        )
                    }
                }
                if (frequency == Frequency.WEEKLY) {
                    OutlinedTextField(
                        value = targetPerWeek.toString(),
                        onValueChange = { input ->
                            targetPerWeek = input.toIntOrNull() ?: 0
                        },
                        label = { Text("每周目标次数") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                // ---- M3 3.1：图标选择（横向滚动，选中高亮）----
                IconPicker(selectedKey = iconRes, onSelect = { iconRes = it })
                // ---- M3 3.1：颜色选择（色板，选中描边）----
                ColorPicker(selectedHex = colorHex, onSelect = { colorHex = it })
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, frequency, targetPerWeek, iconRes, colorHex) },
                enabled = name.isNotBlank(), // 验收：空名称禁点保存
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

/** 图标选择器：横向滚动的可选图标，选中项加主题色描边（M3 3.1） */
@Composable
private fun IconPicker(
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "图标",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier =
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HABIT_ICON_OPTIONS.forEach { option ->
                val selected = option.key == selectedKey
                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                },
                            ).border(
                                width = if (selected) 2.dp else 0.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape,
                            ).clickable { onSelect(option.key) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = option.imageVector,
                        contentDescription = option.label,
                        tint =
                            if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }
        }
    }
}

/** 颜色选择器：色板圆点，选中项加主题色描边（M3 3.1） */
@Composable
private fun ColorPicker(
    selectedHex: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "颜色",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier =
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HABIT_COLOR_OPTIONS.forEach { hex ->
                val selected = hex == selectedHex
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(hex)))
                            .border(
                                width = 2.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape,
                            ).clickable { onSelect(hex) },
                )
            }
        }
    }
}
