package cn.zjl.habitflow.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.LocalDate

/**
 * StreakCalculator excused（补卡/请假）参数化补测（M3 3.6，TECH_DESIGN_v1.3 §5.3）
 *
 * excused 语义（§5.3）：豁免日不计入连续天数、不中断连续性（跳过）；
 * longestStreak 中相邻打卡日间隔内每一天均为豁免日时两段桥接合并。
 * 本类以参数化表格覆盖 excused 与缺勤的对比、连续 excused、首尾 excused 等边界。
 */
@RunWith(Parameterized::class)
class StreakCalculatorExcusedParameterizedTest(
    private val caseName: String,
    private val records: Map<LocalDate, Boolean>,
    private val today: LocalDate,
    private val excused: Set<LocalDate>,
    private val expectedCurrent: Int,
    private val expectedLongest: Int,
) {
    companion object {
        private val today = LocalDate.of(2026, 8, 13)

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> =
            listOf(
                // excused 不中断 current：今天打、昨天豁免、前天打 -> 跳过豁免日连续
                arrayOf(
                    "excused 不中断 current（今打/昨豁免/前天打）",
                    mapOf(today to true, today.minusDays(2) to true),
                    today,
                    setOf(today.minusDays(1)),
                    2,
                    2,
                ),
                // 连续 2 天 excused 不中断：today 打、d-1/d-2 豁免、d-3 打
                arrayOf(
                    "连续 2 天 excused 不中断 current",
                    mapOf(today to true, today.minusDays(3) to true),
                    today,
                    setOf(today.minusDays(1), today.minusDays(2)),
                    2,
                    2,
                ),
                // excused vs 缺勤对比：同样间隔，缺勤断连续（current 仅计今天）
                arrayOf(
                    "缺勤中断 current（与 excused 对比）",
                    mapOf(today to true, today.minusDays(2) to true),
                    today,
                    emptySet<LocalDate>(),
                    1,
                    1,
                ),
                // 首日即 excused（无任何打卡）：跳过豁免日后无打卡日 -> 0
                arrayOf(
                    "首日 excused 且无打卡 -> 0",
                    emptyMap<LocalDate, Boolean>(),
                    today,
                    setOf(today),
                    0,
                    0,
                ),
                // 末日（today）excused + 昨天已打：今天豁免跳过、昨天起算连续
                arrayOf(
                    "末日（today）excused + 昨天打卡 -> current 1",
                    mapOf(today.minusDays(1) to true),
                    today,
                    setOf(today),
                    1,
                    1,
                ),
                // 全 excused 无打卡 -> 0
                arrayOf(
                    "全 excused 无打卡 -> 0",
                    emptyMap<LocalDate, Boolean>(),
                    today,
                    setOf(today.minusDays(1), today.minusDays(2)),
                    0,
                    0,
                ),
                // longest 多日桥接：d1/d2 打、d3/d4 豁免、d5 打 -> 桥接连续 3
                arrayOf(
                    "longest 多日 excused 桥接（2 天豁免）",
                    mapOf(
                        LocalDate.of(2026, 8, 1) to true,
                        LocalDate.of(2026, 8, 2) to true,
                        LocalDate.of(2026, 8, 5) to true,
                    ),
                    LocalDate.of(2026, 8, 5),
                    setOf(LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 4)),
                    3,
                    3,
                ),
            )
    }

    @Test
    fun `excused semantics match table`() {
        assertEquals(caseName, expectedCurrent, StreakCalculator.currentStreak(records, today, excused))
        assertEquals(caseName, expectedLongest, StreakCalculator.longestStreak(records, excused))
    }
}
