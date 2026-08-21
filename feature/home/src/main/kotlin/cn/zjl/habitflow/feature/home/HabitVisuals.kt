package cn.zjl.habitflow.feature.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 习惯图标/颜色预设（M3 3.1：编辑器图标/颜色选择）
 *
 * - [HABIT_ICON_OPTIONS]：图标以字符串 key 存储（[cn.zjl.habitflow.model.Habit.iconRes]），
 *   而非资源 id，避免混淆后资源 id 漂移（TECH_DESIGN §6.1）；
 * - 注意：仅使用 `material-icons-core` 内置图标（本项目未引入 icons-extended，
 *   避免无谓的 APK 体积增长——extended 包约 +20MB）；
 * - [HABIT_COLOR_OPTIONS]：颜色以十六进制字符串存储（[cn.zjl.habitflow.model.Habit.colorHex]）；
 * - 展示时由 [iconFromString] / [colorFromString] 映射回 UI 类型，未知值安全回退。
 */
data class HabitIconOption(
    val key: String,
    val imageVector: ImageVector,
    val label: String,
)

val HABIT_ICON_OPTIONS = listOf(
    HabitIconOption("star", Icons.Filled.Star, "星星"),
    HabitIconOption("favorite", Icons.Filled.Favorite, "爱心"),
    HabitIconOption("checkin", Icons.Filled.CheckCircle, "打卡"),
    HabitIconOption("reminder", Icons.Filled.Notifications, "提醒"),
    HabitIconOption("skill", Icons.Filled.Build, "技能"),
    HabitIconOption("self", Icons.Filled.Face, "自我"),
    HabitIconOption("location", Icons.Filled.Place, "地点"),
    HabitIconOption("shopping", Icons.Filled.ShoppingCart, "购物"),
    HabitIconOption("home", Icons.Filled.Home, "家务"),
    HabitIconOption("schedule", Icons.Filled.DateRange, "日程"),
)

val HABIT_COLOR_OPTIONS = listOf(
    "#2E7D32", "#1565C0", "#C62828", "#F9A825",
    "#6A1B9A", "#00838F", "#AD1457", "#EF6C00", "#37474F",
)

/** key -> ImageVector；未知/空 key 回退到 [Icons.Filled.Star] */
fun iconFromString(key: String?): ImageVector =
    HABIT_ICON_OPTIONS.firstOrNull { it.key == key }?.imageVector ?: Icons.Filled.Star

/**
 * hex -> Color；空或非法值返回 null，调用方再决定回退（如主题 primary）。
 * 解析失败兜底 null 而非抛异常，保证 UI 不崩（防御式渲染）。
 */
fun colorFromString(hex: String?): Color? =
    if (!hex.isNullOrEmpty()) {
        try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: IllegalArgumentException) {
            null
        }
    } else {
        null
    }
