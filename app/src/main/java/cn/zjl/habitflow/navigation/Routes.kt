package cn.zjl.habitflow.navigation

import kotlinx.serialization.Serializable

/**
 * 类型安全路由（TECH_DESIGN_v1.1 §4.5）
 *
 * - 集中在 :app 定义，feature 模块不感知路由实现；
 * - 三个根目的地与 BottomBar 三项一一对应（BottomBar 集成见任务 2.5）；
 * - feature 内部二级页面 MVP 用 Dialog 而非独立目的地，避免嵌套 NavGraph。
 */
@Serializable
data object HomeRoute

@Serializable
data object StatsRoute

@Serializable
data object SettingsRoute
