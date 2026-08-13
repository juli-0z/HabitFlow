package cn.zjl.habitflow.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cn.zjl.habitflow.designsystem.theme.HabitFlowTheme

/** 加载态视图（TECH_DESIGN_v1.1 §1.8 三态组件），页面级居中 Loading */
@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingViewPreview() {
    HabitFlowTheme {
        LoadingView()
    }
}
