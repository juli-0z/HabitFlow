package cn.zjl.habitflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cn.zjl.habitflow.data.entity.HabitEntity
import cn.zjl.habitflow.data.entity.HabitRecordEntity
import cn.zjl.habitflow.data.entity.HabitWithRecords
import kotlinx.coroutines.flow.Flow

/**
 * 习惯与打卡 DAO（TECH_DESIGN_v1.1 §6.1）
 * - 查询全部返回 Flow（响应式，UI 无需手动刷新）；
 * - 写操作全部为 suspend fun。
 */
@Dao
interface HabitDao {
    // ---- 习惯 CRUD（§5.1）----

    /** 新建/编辑共用（@Upsert：id=0 插入，id>0 更新）；返回 rowId */
    @Upsert
    suspend fun upsertHabit(habit: HabitEntity): Long

    /** 软删除（isArchived=1），保留统计数据完整性 */
    @Query("UPDATE habit SET isArchived = 1 WHERE id = :habitId")
    suspend fun archiveHabit(habitId: Long)

    @Query("SELECT * FROM habit WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun observeActiveHabits(): Flow<List<HabitEntity>>

    // ---- 打卡（§5.2）----

    /** 打卡 = 插入当日记录（重复打卡 IGNORE） */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecord(record: HabitRecordEntity): Long

    /** 撤销打卡 = 删除当日记录 */
    @Query("DELETE FROM habit_record WHERE habit_id = :habitId AND date = :date")
    suspend fun deleteRecord(
        habitId: Long,
        date: Long,
    )

    /** 当日"是否已打卡"，Flow 响应式自动刷新 */
    @Query("SELECT EXISTS(SELECT 1 FROM habit_record WHERE habit_id = :habitId AND date = :date)")
    fun observeChecked(
        habitId: Long,
        date: Long,
    ): Flow<Boolean>

    // ---- 统计（§5.4 / §6.1 组合查询）----

    @Transaction
    @Query("SELECT * FROM habit WHERE isArchived = 0")
    fun observeHabitsWithRecords(): Flow<List<HabitWithRecords>>

    @Query("SELECT * FROM habit_record WHERE habit_id = :habitId AND date BETWEEN :startDate AND :endDate")
    fun observeRecordsBetween(
        habitId: Long,
        startDate: Long,
        endDate: Long,
    ): Flow<List<HabitRecordEntity>>
}
