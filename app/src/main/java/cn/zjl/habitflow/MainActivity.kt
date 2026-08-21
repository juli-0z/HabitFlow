package cn.zjl.habitflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import cn.zjl.habitflow.designsystem.theme.HabitFlowTheme
import cn.zjl.habitflow.feature.home.HomeScreen
import cn.zjl.habitflow.feature.home.HomeViewModel
import cn.zjl.habitflow.feature.settings.SettingsScreen
import cn.zjl.habitflow.feature.settings.SettingsViewModel
import cn.zjl.habitflow.feature.stats.StatsScreen
import cn.zjl.habitflow.feature.stats.StatsViewModel
import cn.zjl.habitflow.navigation.HabitFlowBottomBar
import cn.zjl.habitflow.navigation.HomeRoute
import cn.zjl.habitflow.navigation.SettingsRoute
import cn.zjl.habitflow.navigation.StatsRoute
import dagger.hilt.android.AndroidEntryPoint

/**
 * 单 Activity（TECH_DESIGN_v1.2 §4.5）：Scaffold + BottomBar + NavHost。
 *
 * - BottomBar 选中态：currentBackStackEntryAsState + hierarchy（见 BottomBar.kt）；
 * - Tab 切换：popUpTo(startDestination){saveState} + restoreState——三页 ViewModel
 *   状态保留、切换不销毁（NavBackStackEntry 与 ViewModelStore 绑定，面试考点）；
 * - VM 在导航装配层获取（§4.2：Screen 禁止内部 hiltViewModel）。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 深色模式全局生效（2.6，§11.5 双轨）：activity 级 VM 状态驱动主题
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            HabitFlowTheme(darkTheme = settingsState.isDarkMode) {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()

                Scaffold(
                    bottomBar = {
                        HabitFlowBottomBar(
                            currentDestination = backStackEntry?.destination,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    // BottomBar 标准模式：回起点保存/恢复状态，避免 VM 销毁与栈堆积
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    },
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute,
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        composable<HomeRoute> {
                            val viewModel: HomeViewModel = hiltViewModel()
                            HomeScreen(viewModel = viewModel)
                        }
                        composable<StatsRoute> {
                            val viewModel: StatsViewModel = hiltViewModel()
                            StatsScreen(viewModel = viewModel)
                        }
                        composable<SettingsRoute> {
                            // 目的地作用域 VM（§4.2：导航装配层获取；与 activity 级主题 VM 同源 DataStore）
                            val settingsViewModel: SettingsViewModel = hiltViewModel()
                            SettingsScreen(viewModel = settingsViewModel)
                        }
                    }
                }
            }
        }
    }
}
