package cn.zjl.habitflow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.zjl.habitflow.model.Frequency

/**
 * 习惯表实体（TECH_DESIGN_v1.1 §6.1）
 *
 * iconRes/colorHex 存字符串而非资源 id，避免混淆后资源 id 漂移。
 */
@Entity(tableName = "habit")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val frequency: Frequency, // DAILY / WEEKLY
    val targetPerWeek: Int = 0, // WEEKLY 时有效
    val iconRes: String? = null, // 存资源名而非资源 id
    val colorHex: String = "", // 同上，存字符串
    val isArchived: Boolean = false,
    val createdAt: Long,
)
