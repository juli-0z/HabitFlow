package cn.zjl.habitflow.model

/**
 * 习惯聚合模型（纯业务模型，:core:model 无任何 Android 依赖）
 *
 * 字段与数据层 HabitEntity 对齐（TECH_DESIGN_v1.1 §6.1），
 * entity -> model 映射由 Repository 层负责（§6.4）。
 */
data class Habit(
    val id: Long = 0,
    val name: String,
    val frequency: Frequency,
    val targetPerWeek: Int = 0, // WEEKLY 时有效（1~7）
    val iconRes: String? = null, // 存资源名而非资源 id，避免混淆后资源 id 漂移
    val colorHex: String = "", // 同上，存字符串
    val isArchived: Boolean = false,
    val createdAt: Long = 0L, // epochMillis
)
