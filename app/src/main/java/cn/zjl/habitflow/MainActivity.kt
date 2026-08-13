package cn.zjl.habitflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint

/**
 * 单 Activity（TECH_DESIGN_v1.1 §4.5）：仅承载 NavHost。
 *
 * 本轮为最小可运行迁移：内容为空壳（Compose 启动即达），
 * NavHost + BottomBar 接入见任务 1.12；theme 由 :core:designsystem 接管见任务 1.8。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("HabitFlow")
                    }
                }
            }
        }
    }
}
