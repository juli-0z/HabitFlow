package cn.zjl.habitflow.model

/**
 * 统计页聚合数据（TECH_DESIGN_v1.1 §1.3/§5.4）
 *
 * 完成率 = completedDays / totalDays（totalDays 排除未开始的未来日期），由 :feature:stats 计算后填充。
 */
data class StreakStats(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val sevenDayCompletionRate: Double = 0.0,
    val thirtyDayCompletionRate: Double = 0.0,
)
