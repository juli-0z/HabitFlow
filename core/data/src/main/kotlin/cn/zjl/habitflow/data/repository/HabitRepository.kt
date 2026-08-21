package cn.zjl.habitflow.data.repository

import cn.zjl.habitflow.model.Habit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 习惯仓库接口（TECH_DESIGN_v1.1 §6.3：接口 + 实现均在 :core:data，Hilt @Binds 绑定）
 */
interface HabitRepository {
    /** 未归档习惯列表（§5.1，含软删除过滤） */
    fun observeHabits(): Flow<List<Habit>>

    /** 当日"是否已打卡"（§5.2，Flow 响应式） */
    fun observeChecked(
        habitId: Long,
        date: LocalDate,
    ): Flow<Boolean>

    /** 近 [days] 天完成情况（§5.4 统计数据来源，每天一个条目，未打卡=false） */
    fun observeCompletionStats(
        habitId: Long,
        days: Int,
    ): Flow<List<Pair<LocalDate, Boolean>>>

    /** 新建/编辑（upsert，id=0 插入），返回 rowId */
    suspend fun saveHabit(habit: Habit): Long

    /** 软删除（§5.1） */
    suspend fun archiveHabit(habitId: Long)

    /** 打卡 = 插入当日记录（§5.2） */
    suspend fun checkIn(
        habitId: Long,
        date: LocalDate,
    )

    /** 撤销打卡 = 删除当日记录（§5.2） */
    suspend fun checkOut(
        habitId: Long,
        date: LocalDate,
    )
}
