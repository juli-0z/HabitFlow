package cn.zjl.habitflow.feature.settings

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 每日提醒调度器（M3 3.8，TECH_DESIGN §5：WorkManager PeriodicWorkRequest）
 *
 * - 固定每日 [REMINDER_HOUR]:00 触发（不追求精确时刻，InitialDelay 对齐目标时间，§5 不做时间选择器）；
 * - 周期 24h；KEEP 策略避免重复入队；关闭时取消唯一任务。
 */
@Singleton
class ReminderScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val workManager: WorkManager
            get() = WorkManager.getInstance(context)

        /** 开启每日提醒：周期 24h，InitialDelay 对齐到下一个 [REMINDER_HOUR]:00 */
        fun enable() {
            val now = LocalDateTime.now()
            val targetToday = now.toLocalDate().atTime(REMINDER_HOUR, 0)
            val target = if (targetToday.isAfter(now)) targetToday else targetToday.plusDays(1)
            val initialDelayMinutes = Duration.between(now, target).toMinutes().coerceAtLeast(1)
            val request =
                PeriodicWorkRequestBuilder<HabitReminderWorker>(PERIOD_HOURS, TimeUnit.HOURS)
                    .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
                    .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        /** 关闭每日提醒：取消唯一周期任务 */
        fun disable() {
            workManager.cancelUniqueWork(WORK_NAME)
        }

        private companion object {
            const val WORK_NAME = "habit_reminder_daily"
            const val REMINDER_HOUR = 20 // 每日 20:00 默认值（不做时间选择器，§5 最小范围）
            const val PERIOD_HOURS = 24L
        }
    }
