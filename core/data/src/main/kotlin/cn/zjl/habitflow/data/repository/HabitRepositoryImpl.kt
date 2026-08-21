package cn.zjl.habitflow.data.repository

import cn.zjl.habitflow.data.dao.HabitDao
import cn.zjl.habitflow.data.entity.HabitEntity
import cn.zjl.habitflow.data.entity.HabitRecordEntity
import cn.zjl.habitflow.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * 习惯仓库实现（TECH_DESIGN_v1.1 §6.4：entity -> model 映射在 Repository 层）
 */
class HabitRepositoryImpl
    @Inject
    constructor(
        private val habitDao: HabitDao,
    ) : HabitRepository {
        override fun observeHabits(): Flow<List<Habit>> = habitDao.observeActiveHabits().map { list -> list.map { it.toModel() } }

        override fun observeChecked(
            habitId: Long,
            date: LocalDate,
        ): Flow<Boolean> = habitDao.observeChecked(habitId, date.toEpochDay())

        override fun observeCompletionStats(
            habitId: Long,
            days: Int,
        ): Flow<List<Pair<LocalDate, Boolean>>> {
            val today = LocalDate.now()
            return habitDao
                .observeRecordsBetween(
                    habitId = habitId,
                    startDate = today.minusDays((days - 1).toLong()).toEpochDay(),
                    endDate = today.toEpochDay(),
                ).map { records ->
                    val checkedDays = records.mapTo(mutableSetOf()) { LocalDate.ofEpochDay(it.date) }
                    (0 until days).map { offset ->
                        val date = today.minusDays((days - 1 - offset).toLong())
                        date to (date in checkedDays)
                    }
                }
        }

        override suspend fun saveHabit(habit: Habit): Long = habitDao.upsertHabit(habit.toEntity())

        override suspend fun archiveHabit(habitId: Long) = habitDao.archiveHabit(habitId)

        override suspend fun checkIn(
            habitId: Long,
            date: LocalDate,
        ) {
            habitDao.insertRecord(
                HabitRecordEntity(
                    habitId = habitId,
                    date = date.toEpochDay(),
                    completedAt = System.currentTimeMillis(),
                ),
            )
        }

        override suspend fun checkOut(
            habitId: Long,
            date: LocalDate,
        ) = habitDao.deleteRecord(habitId, date.toEpochDay())
    }

private fun HabitEntity.toModel() =
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

private fun Habit.toEntity() =
    HabitEntity(
        id = id,
        name = name,
        frequency = frequency,
        targetPerWeek = targetPerWeek,
        iconRes = iconRes,
        colorHex = colorHex,
        isArchived = isArchived,
        createdAt = createdAt,
    )
