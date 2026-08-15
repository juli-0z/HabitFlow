package cn.zjl.habitflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cn.zjl.habitflow.designsystem.theme.HabitFlowTheme
import cn.zjl.habitflow.feature.home.HomeScreen
import cn.zjl.habitflow.feature.settings.SettingsScreen
import cn.zjl.habitflow.feature.stats.StatsScreen
import cn.zjl.habitflow.navigation.HomeRoute
import cn.zjl.habitflow.navigation.SettingsRoute
import cn.zjl.habitflow.navigation.StatsRoute
import dagger.hilt.android.AndroidEntryPoint

/**
 * 单 Activity（TECH_DESIGN_v1.1 §4.5）：仅承载 NavHost。
 *
 * 任务 1.12：NavHost 骨架 + 三个 feature 根目的地（占位 Screen，类型安全路由）；
 * BottomBar 集成见任务 2.5（§4.5：currentBackStackEntryAsState 同步选中态）。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitFlowTheme {
                val navController = rememberNavController()
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute,
                    ) {
                        composable<HomeRoute> { HomeScreen() }
                        composable<StatsRoute> { StatsScreen() }
                        composable<SettingsRoute> { SettingsScreen() }
                    }
                }
            }
        }
    }
}
