package cn.zjl.habitflow.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cn.zjl.habitflow.model.Frequency
import cn.zjl.habitflow.model.Habit

/**
 * 新建/编辑习惯弹窗（TECH_DESIGN_v1.2 §5.1）
 *
 * - 新建与编辑共用同一表单与校验（HabitValidator 在 ViewModel 层调用，§5.1）；
 * - 空名称时保存按钮禁用（任务 2.3 验收），其余校验失败（如 WEEKLY 目标次数越界）
 *   由 ViewModel 回传 [errorMessage] 在弹窗内展示；
 * - 表单状态用 rememberSaveable（配置变更不丢输入，§4.5 状态恢复约定）。
 */
@Composable
fun HabitEditorDialog(
    initialHabit: Habit?,
    errorMessage: String?,
    onSave: (name: String, frequency: Frequency, targetPerWeek: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf(initialHabit?.name.orEmpty()) }
    var frequency by rememberSaveable { mutableStateOf(initialHabit?.frequency ?: Frequency.DAILY) }
    var targetPerWeek by rememberSaveable { mutableIntStateOf(initialHabit?.targetPerWeek?.coerceAtLeast(1) ?: 1) }

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
                onClick = { onSave(name, frequency, targetPerWeek) },
                enabled = name.isNotBlank(),   // 验收：空名称禁点保存
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
