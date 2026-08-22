package cn.zjl.habitflow.feature.settings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 习惯打卡提醒 Worker（M3 3.8，TECH_DESIGN §5：WorkManager + @HiltWorker 集成）
 *
 * - 每日周期触发（ReminderScheduler 调度，不追求精确时刻——WorkManager Doze 调度不确定性，练习场景可接受）；
 * - 创建通知渠道（API 26+）+ 发送提醒通知；
 * - API 33+（TIRAMISU）未授予 POST_NOTIFICATIONS 时静默跳过（降级为无通知模式）。
 */
@HiltWorker
class HabitReminderWorker
    @AssistedInject
    constructor(
        @Assisted private val appContext: Context,
        @Assisted workerParams: WorkerParameters,
    ) : Worker(appContext, workerParams) {
        override fun doWork(): Result {
            showReminderNotification()
            return Result.success()
        }

        private fun showReminderNotification() {
            val notificationManager = appContext.getSystemService(NotificationManager::class.java)
            // API 33+ 未授予通知权限 -> 静默跳过（降级，§5 风险预案）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationManager.areNotificationsEnabled()) {
                return
            }
            // API 26+ 创建通知渠道（minSdk 24 需版本判断）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel =
                    NotificationChannel(CHANNEL_ID, "习惯打卡提醒", NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }
            val notification =
                NotificationCompat
                    .Builder(appContext, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("习惯打卡提醒")
                    .setContentText("别忘了完成今天的习惯打卡哦")
                    .setAutoCancel(true)
                    .build()
            notificationManager.notify(NOTIFICATION_ID, notification)
        }

        companion object {
            const val CHANNEL_ID = "habit_reminder"
            const val NOTIFICATION_ID = 1001
        }
    }
