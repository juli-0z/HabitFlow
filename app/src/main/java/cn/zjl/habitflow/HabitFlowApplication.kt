package cn.zjl.habitflow

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用入口（TECH_DESIGN_v1.1 §11.5：@HiltAndroidApp，顶层 DI 装配点）
 */
@HiltAndroidApp
class HabitFlowApplication : Application()
