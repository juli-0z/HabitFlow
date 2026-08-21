package cn.zjl.habitflow.testing

import cn.zjl.habitflow.model.Frequency
import cn.zjl.habitflow.model.Habit
import cn.zjl.habitflow.model.HabitRecord
import java.time.LocalDate

/**
 * 测试数据工厂（TECH_DESIGN_v1.1 §1.10/§8.4，放 :core:testing）
 *
 * 供各模块 test/androidTest 源码集复用；默认值设计保证"无参即可用"，
 * 只覆盖被测场景所需的字段（2.7 ViewModel 测试、3.9 Repository 映射测试等）。
 */
object TestDataFactory {
    fun habit(
        id: Long = 0,
        name: String = "晨跑",
        frequency: Frequency = Frequency.DAILY,
        targetPerWeek: Int = 0,
        iconRes: String? = null,
        colorHex: String = "FF2E7D32",
        isArchived: Boolean = false,
        createdAt: Long = DEFAULT_TIMESTAMP,
    ): Habit =
        Habit(
            id = id,
            name = name,
            frequency = frequency,
            targetPerWeek = targetPerWeek,
            iconRes = iconRes,
            colorHex = colorHex,
            isArchived = isArchived,
            createdAt = createdAt,
        )

    fun habitRecord(
        id: Long = 0,
        habitId: Long = 1,
        date: LocalDate = DEFAULT_DATE,
        completedAt: Long = DEFAULT_TIMESTAMP,
        isExcused: Boolean = false,
        note: String? = null,
    ): HabitRecord =
        HabitRecord(
            id = id,
            habitId = habitId,
            date = date,
            completedAt = completedAt,
            isExcused = isExcused,
            note = note,
        )

    private val DEFAULT_DATE: LocalDate = LocalDate.of(2026, 8, 13)

    private const val DEFAULT_TIMESTAMP = 0L
}
