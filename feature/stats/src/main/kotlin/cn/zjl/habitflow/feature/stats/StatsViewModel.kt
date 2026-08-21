package cn.zjl.habitflow.feature.stats

import androidx.lifecycle.viewModelScope
import cn.zjl.habitflow.data.repository.HabitRepository
import cn.zjl.habitflow.designsystem.base.BaseViewModel
import cn.zjl.habitflow.designsystem.base.toUserMessage
import cn.zjl.habitflow.domain.StreakCalculator
import cn.zjl.habitflow.model.Frequency
import cn.zjl.habitflow.model.Habit
import cn.zjl.habitflow.model.StreakStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 统计页 ViewModel（M3 3.4，§5.4）
 *
 * - 每个未归档习惯一次查询 [WINDOW_DAYS] 天完成情况（§5.4 observeCompletionStats），
 *   派生：近 7/30 天完成率（窗口内已打卡/窗口天数）+ 当前/最长连续（StreakCalculator，§5.3）；
 * - 完成率口径：窗口内未打卡日期计未完成（totalDays = 窗口天数，§5.4）。
 */
@HiltViewModel
class StatsViewModel
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
    ) : BaseViewModel() {
        private val _uiState = MutableStateFlow(StatsUiState(isLoading = true))
        val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

        init {
            observeStats()
        }

        private fun observeStats() {
            viewModelScope.launch {
                habitRepository
                    .observeHabits()
                    .flatMapLatest { habits -> observeStatsForHabits(habits) }
                    .catch { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) }
                    }.collect { (stats, heatmap) ->
                        _uiState.update { it.copy(isLoading = false, stats = stats, heatmap = heatmap) }
                    }
            }
        }

        /** 合并各习惯统计 + 热力图聚合（combine，顺序与 habits 一致；M3 3.5） */
        private fun observeStatsForHabits(habits: List<Habit>): kotlinx.coroutines.flow.Flow<Pair<List<HabitStats>, List<HeatmapCell>>> {
            if (habits.isEmpty()) return flowOf(emptyList<HabitStats>() to emptyList())
            val today = LocalDate.now()
            return combine(
                habits.map { habit ->
                    habitRepository.observeCompletionStats(habit.id, WINDOW_DAYS).map { days ->
                        habit to days
                    }
                },
            ) { values ->
                val pairs = values.toList()
                val stats =
                    pairs.map { (habit, days) ->
                        HabitStats(habit = habit, stats = computeStats(days, today, habit))
                    }
                stats to aggregateHeatmap(pairs.map { it.second }, today)
            }
        }

        /** 热力图聚合：统计窗口内每天被多少个习惯完成（0..N，M3 3.5，颜色分级依据） */
        private fun aggregateHeatmap(
            windows: List<List<Pair<LocalDate, Boolean>>>,
            today: LocalDate,
        ): List<HeatmapCell> {
            if (windows.isEmpty()) return emptyList()
            val countByDate = mutableMapOf<LocalDate, Int>()
            windows.forEach { window ->
                window.forEach { (date, checked) ->
                    if (checked) countByDate[date] = (countByDate[date] ?: 0) + 1
                }
            }
            val start = today.minusDays((WINDOW_DAYS - 1).toLong())
            return (0 until WINDOW_DAYS).map { offset ->
                val date = start.plusDays(offset.toLong())
                HeatmapCell(date = date, completedCount = countByDate[date] ?: 0)
            }
        }

        private fun computeStats(
            days: List<Pair<LocalDate, Boolean>>,
            today: LocalDate,
            habit: Habit,
        ): StreakStats {
            val records = days.toMap()
            // longestStreak 契约：只传已打卡日期（keys 视为打卡日，§5.3），未打卡日必须剔除
            val checkedDates = days.filter { it.second }.map { it.first }.toSet()
            return StreakStats(
                currentStreak = StreakCalculator.currentStreak(records, today),
                longestStreak = StreakCalculator.longestStreak(checkedDates.associateWith { true }),
                sevenDayCompletionRate = completionRate(days.takeLast(7), habit),
                thirtyDayCompletionRate = completionRate(days.takeLast(30), habit),
            )
        }

        /**
         * 完成率（M3 3.7：区分频率）。
         * DAILY：分母 = 窗口天数（每天应打卡）；
         * WEEKLY：分母 = targetPerWeek × 窗口折合周数（每周目标 targetPerWeek 次），完成率封顶 1.0。
         */
        private fun completionRate(
            window: List<Pair<LocalDate, Boolean>>,
            habit: Habit,
        ): Double {
            if (window.isEmpty()) return 0.0
            val checked = window.count { it.second }.toDouble()
            val target =
                when (habit.frequency) {
                    Frequency.DAILY -> window.size.toDouble()
                    Frequency.WEEKLY -> {
                        val weeks = window.size / 7.0
                        (habit.targetPerWeek * weeks).coerceAtLeast(1.0)
                    }
                }
            return (checked / target).coerceAtMost(1.0)
        }

        private companion object {
            /** 统计查询窗口：覆盖 30 天完成率 + 足够长的最长连续（与首页连续窗口 60 一致，MVP 取舍） */
            const val WINDOW_DAYS = 60
        }
    }

/** 单个习惯的统计条目（页面级 data class，ViewModel 同文件定义，§4.2） */
data class HabitStats(
    val habit: Habit,
    val stats: StreakStats,
)

/** 热力图单元格：日期 + 当日完成习惯数（M3 3.5，聚合各习惯，颜色分级依据） */
data class HeatmapCell(
    val date: LocalDate,
    val completedCount: Int,
)

/** 统计页状态（§4.2） */
data class StatsUiState(
    val stats: List<HabitStats> = emptyList(),
    val heatmap: List<HeatmapCell> = emptyList(), // 近 WINDOW_DAYS 天每日完成习惯数（M3 3.5）
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
