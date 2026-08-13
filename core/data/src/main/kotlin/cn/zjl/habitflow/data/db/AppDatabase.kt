package cn.zjl.habitflow.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cn.zjl.habitflow.data.converters.Converters
import cn.zjl.habitflow.data.dao.HabitDao
import cn.zjl.habitflow.data.entity.HabitEntity
import cn.zjl.habitflow.data.entity.HabitRecordEntity

/**
 * 应用数据库（TECH_DESIGN_v1.1 §6.1）
 * - exportSchema = true，schemas/ 目录进 Git，迁移时 diff 校验；
 * - 禁止 fallbackToDestructiveMigration（文档写死，代码评审照此检查）。
 */
@Database(
    entities = [HabitEntity::class, HabitRecordEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)   // 备选方案（LocalDate 字段）时启用
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}
