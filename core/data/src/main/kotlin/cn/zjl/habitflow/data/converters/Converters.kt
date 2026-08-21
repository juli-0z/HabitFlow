package cn.zjl.habitflow.data.converters

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * TypeConverter 备选方案（TECH_DESIGN_v1.1 §5.2/§6.1）：
 * 主方案 date 以 Long(epochDay) 直存，无需本转换器；
 * 若改用 LocalDate 字段则启用（@Database 已注册 @TypeConverters）。
 */
class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long): LocalDate = LocalDate.ofEpochDay(value)

    @TypeConverter
    fun toEpochDay(date: LocalDate): Long = date.toEpochDay()
}
