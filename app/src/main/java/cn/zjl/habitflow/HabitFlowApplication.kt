package cn.zjl.habitflow

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * 应用入口（TECH_DESIGN_v1.1 §11.5：@HiltAndroidApp，顶层 DI 装配点）
 *
 * M3 3.8：implements Configuration.Provider——WorkManager + Hilt 集成，
 * 使用 [HiltWorkerFactory] 构造 @HiltWorker（HabitReminderWorker），需配合 Manifest
 * 禁用默认 WorkManager 初始化（androidx.startup remove）。
 */
@HiltAndroidApp
class HabitFlowApplication :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(workerFactory)
                .build()
}
