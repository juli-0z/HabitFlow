package cn.zjl.habitflow.model

/**
 * 打卡频率类型（TECH_DESIGN_v1.1 §5.1/§6.1）
 *
 * DAILY：每日型（MVP 核心）；WEEKLY：每周 N 次型（字段进入表结构，完整校验与统计后置）。
 */
enum class Frequency {
    DAILY,
    WEEKLY,
}
