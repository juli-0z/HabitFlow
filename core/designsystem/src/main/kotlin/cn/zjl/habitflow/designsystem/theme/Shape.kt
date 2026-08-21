package cn.zjl.habitflow.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** 形状体系（TECH_DESIGN_v1.1 §3.1）：统一圆角，组件复用 */
val HabitFlowShapes =
    Shapes(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
    )
