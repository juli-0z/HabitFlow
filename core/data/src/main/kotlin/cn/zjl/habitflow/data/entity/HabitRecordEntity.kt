package cn.zjl.habitflow.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 打卡记录表实体（TECH_DESIGN_v1.1 §6.1）
 *
 * - date 以 Long(epochDay) 直接存储（§5.2），避免时区问题；
 * - isExcused 为补卡/请假预留字段（§5.3 扩展点）。
 */
@Entity(
    tableName = "habit_record",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habit_id"), Index("date")],
)
data class HabitRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val date: Long, // epochDay
    val completedAt: Long, // 打卡时间戳（epochMillis）
    val isExcused: Boolean = false,
)
