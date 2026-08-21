package cn.zjl.habitflow.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// 品牌色板（习惯打卡：健康绿系；TECH_DESIGN_v1.1 §3.1 Theme 体系）
private val GreenPrimary = Color(0xFF2E7D32)
private val GreenSecondary = Color(0xFF558B2F)
private val GreenTertiary = Color(0xFF00695C)

private val GreenPrimaryDark = Color(0xFF81C784)
private val GreenSecondaryDark = Color(0xFFA5D6A7)
private val GreenTertiaryDark = Color(0xFF4DB6AC)

/** 浅色 ColorScheme（Material3 lightColorScheme 派生） */
val LightColorScheme =
    lightColorScheme(
        primary = GreenPrimary,
        secondary = GreenSecondary,
        tertiary = GreenTertiary,
    )

/** 深色 ColorScheme（Material3 darkColorScheme 派生） */
val DarkColorScheme =
    darkColorScheme(
        primary = GreenPrimaryDark,
        secondary = GreenSecondaryDark,
        tertiary = GreenTertiaryDark,
    )
