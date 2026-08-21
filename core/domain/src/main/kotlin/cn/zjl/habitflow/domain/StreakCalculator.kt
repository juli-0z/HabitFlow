package cn.zjl.habitflow.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Streak（连续天数）计算器——纯函数，最高优先级单测对象（TECH_DESIGN_v1.1 §5.3）。
 *
 * 语义约定：
 * - [records]：日期 -> 是否已打卡；未出现的日期视为未打卡；
 * - [excusedDates]：豁免日期（补卡/请假，后置功能预留）。豁免日不计入连续天数，
 *   但在连续性判断中被跳过（不中断连续），可用于桥接两段打卡；
 * - currentStreak：从今天（或昨天，当天未打时）向前连续打卡天数——
 *   "今天未打卡但昨天已打卡"时从昨天起算（业界惯例：今天的"进行中"连续不中断）；
 * - 连续性判断使用 LocalDate.plusDays/minusDays，天然支持跨月/跨年，不依赖月份/年份逻辑。
 */
object StreakCalculator {
    fun currentStreak(
        records: Map<LocalDate, Boolean>,
        today: LocalDate,
        excusedDates: Set<LocalDate> = emptySet(),
    ): Int {
        var streak = 0
        // 今天未打卡且未豁免 -> 从昨天起算（进行中连续不中断）
        var cursor = today
        if (records[today] != true && today !in excusedDates) {
            cursor = today.minusDays(1)
        }
        while (true) {
            when {
                cursor in excusedDates -> cursor = cursor.minusDays(1) // 豁免：跳过，不中断
                records[cursor] == true -> {
                    streak++
                    cursor = cursor.minusDays(1)
                }
                else -> break
            }
        }
        return streak
    }

    fun longestStreak(
        records: Map<LocalDate, Boolean>,
        excusedDates: Set<LocalDate> = emptySet(),
    ): Int {
        var max = 0
        var current = 0
        var prev: LocalDate? = null
        for (date in records.keys.sorted()) {
            current =
                if (prev == null) {
                    1
                } else {
                    val gap = ChronoUnit.DAYS.between(prev, date)
                    // 相邻打卡日间隔 1 天，或间隔中的每一天都是豁免日（桥接）-> 连续
                    val gapAllExcused = (1L until gap).all { prev.plusDays(it) in excusedDates }
                    if (gap == 1L || gapAllExcused) current + 1 else 1
                }
            max = maxOf(max, current)
            prev = date
        }
        return max
    }
}
