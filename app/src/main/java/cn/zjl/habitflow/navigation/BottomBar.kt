package cn.zjl.habitflow.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy

/**
 * 底部导航栏（TECH_DESIGN_v1.2 §4.5）
 *
 * - 三个 Tab 与 Routes.kt 的根目的地一一对应（feature 不感知路由实现）；
 * - 选中态：`NavDestination.hierarchy` + `hasRoute` 判断当前 back stack 目的地
 *   是否属于某路由（类型安全，§4.5）。
 */
@Composable
fun HabitFlowBottomBar(
    currentDestination: NavDestination?,
    onNavigate: (route: Any) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        bottomBarItems.forEach { item ->
            val selected =
                currentDestination
                    ?.hierarchy
                    ?.any { it.hasRoute(item.route::class) } == true
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) },
            )
        }
    }
}

private data class BottomBarItem(
    val route: Any, // @Serializable 路由对象（单例，可用作 key）
    val label: String,
    val icon: ImageVector,
)

private val bottomBarItems =
    listOf(
        BottomBarItem(route = HomeRoute, label = "首页", icon = Icons.Filled.Home),
        BottomBarItem(route = StatsRoute, label = "统计", icon = Icons.Filled.DateRange),
        BottomBarItem(route = SettingsRoute, label = "设置", icon = Icons.Filled.Settings),
    )
