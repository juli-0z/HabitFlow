package cn.zjl.habitflow.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 习惯 + 打卡记录组合（TECH_DESIGN_v1.1 §6.1：@Transaction + @Relation 组合查询）
 */
data class HabitWithRecords(
    @Embedded val habit: HabitEntity,
    @Relation(parentColumn = "id", entityColumn = "habit_id")
    val records: List<HabitRecordEntity>,
)
