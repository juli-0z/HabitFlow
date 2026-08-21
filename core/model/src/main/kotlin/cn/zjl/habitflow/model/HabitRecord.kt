package cn.zjl.habitflow.model

import java.time.LocalDate

/**
 * 打卡记录（TECH_DESIGN_v1.1 §5.2/§6.1）
 *
 * - date 使用 [LocalDate]：数据层以 epochDay(Long) 存储，Repository 层映射（§5.2）；
 * - isExcused 为补卡/请假预留字段（§5.3 扩展点，MVP 不实现 UI）；
 * - note 为 §5.2 提及的预留字段，数据层实体暂未包含（映射时暂不落库）。
 */
data class HabitRecord(
    val id: Long = 0,
    val habitId: Long,
    val date: LocalDate,
    val completedAt: Long, // 打卡时间戳（epochMillis）
    val isExcused: Boolean = false,
    val note: String? = null,
)
