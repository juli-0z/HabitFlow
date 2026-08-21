package cn.zjl.habitflow.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * StreakCalculator 边界用例（TECH_DESIGN_v1.1 §5.3 五类边界 + excused 扩展点）
 * 纯 JVM 测试，零依赖跑通，作为 CI 第一道门。
 */
class StreakCalculatorTest {
    private val today = LocalDate.of(2026, 8, 13)

    // 边界 1：空数据 -> 0
    @Test
    fun `empty records returns zero for both streaks`() {
        assertEquals(0, StreakCalculator.currentStreak(emptyMap(), today))
        assertEquals(0, StreakCalculator.longestStreak(emptyMap()))
    }

    // 边界 2：今天未打卡但昨天已打卡 -> current 从昨天起算（进行中连续不中断）
    @Test
    fun `current streak counts from yesterday when today not checked`() {
        val records =
            mapOf(
                today.minusDays(1) to true,
                today.minusDays(2) to true,
                today.minusDays(3) to true,
            )
        assertEquals(3, StreakCalculator.currentStreak(records, today))
    }

    // 边界 3：中间断一天 -> current 归零重新起算
    @Test
    fun `current streak resets when a day in between is missed`() {
        val records =
            mapOf(
                today to true,
                today.minusDays(1) to true,
                today.minusDays(3) to true, // 前天断档
            )
        assertEquals(2, StreakCalculator.currentStreak(records, today))
    }

    // 边界 4a：跨月连续（2026-01-31 -> 2026-02-01）
    @Test
    fun `streak continues across month boundary`() {
        val records =
            mapOf(
                LocalDate.of(2026, 2, 1) to true,
                LocalDate.of(2026, 1, 31) to true,
                LocalDate.of(2026, 1, 30) to true,
            )
        assertEquals(3, StreakCalculator.currentStreak(records, LocalDate.of(2026, 2, 1)))
        assertEquals(3, StreakCalculator.longestStreak(records))
    }

    // 边界 4b：跨年连续（2025-12-31 -> 2026-01-01）
    @Test
    fun `streak continues across year boundary`() {
        val records =
            mapOf(
                LocalDate.of(2026, 1, 1) to true,
                LocalDate.of(2025, 12, 31) to true,
            )
        assertEquals(2, StreakCalculator.currentStreak(records, LocalDate.of(2026, 1, 1)))
        assertEquals(2, StreakCalculator.longestStreak(records))
    }

    // 边界 5a：excused 豁免日不中断 current streak，且不计入天数
    @Test
    fun `excused date does not break current streak`() {
        val records =
            mapOf(
                today to true,
                today.minusDays(2) to true,
            )
        val excused = setOf(today.minusDays(1))
        assertEquals(2, StreakCalculator.currentStreak(records, today, excused))
    }

    // 边界 5b：excused 豁免日桥接两段，longest streak 合并
    @Test
    fun `excused date bridges two segments in longest streak`() {
        val records =
            mapOf(
                LocalDate.of(2026, 8, 1) to true,
                LocalDate.of(2026, 8, 2) to true,
                LocalDate.of(2026, 8, 4) to true,
                LocalDate.of(2026, 8, 5) to true,
            )
        val excused = setOf(LocalDate.of(2026, 8, 3))
        assertEquals(4, StreakCalculator.longestStreak(records, excused))
        // 不豁免时两段独立
        assertEquals(2, StreakCalculator.longestStreak(records))
    }

    // longest 补充：多段取最长
    @Test
    fun `longest streak picks the longest segment`() {
        val records =
            mapOf(
                LocalDate.of(2026, 8, 1) to true,
                LocalDate.of(2026, 8, 2) to true,
                LocalDate.of(2026, 8, 3) to true,
                LocalDate.of(2026, 8, 10) to true,
                LocalDate.of(2026, 8, 11) to true,
            )
        assertEquals(3, StreakCalculator.longestStreak(records))
    }

    // 边界 2 补充：今天已打卡时从今天起算（含昨天未打的情况）
    @Test
    fun `current streak starts from today when checked`() {
        val records =
            mapOf(
                today to true,
                today.minusDays(2) to true, // 昨天未打
            )
        assertEquals(1, StreakCalculator.currentStreak(records, today))
    }
}
