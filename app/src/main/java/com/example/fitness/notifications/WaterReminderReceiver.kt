package com.example.fitness.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.fitness.MainActivity
import com.example.fitness.R
import com.example.fitness.data.PlanRepository

/**
 * 系统闹钟触发的 BroadcastReceiver：发出"喝水"通知，并调度下一次闹钟。
 */
class WaterReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val planRepo = PlanRepository.get(context)
        val remaining: Int = planRepo.waterReminderRemaining.value
        if (remaining <= 0) {
            planRepo.setWaterReminderActive(false)
            return
        }

        // 1. 发通知
        showNotification(context, planRepo)

        // 2. 自减
        val nextRemaining: Int = remaining - 1
        planRepo.setWaterReminderRemaining(nextRemaining)

        // 3. 调度下一次
        if (nextRemaining > 0) {
            val nextTriggerAt = System.currentTimeMillis() + planRepo.waterReminderIntervalMillis
            planRepo.setWaterReminderStartEpochMillis(nextTriggerAt)
            WaterReminderScheduler.scheduleNext(context, nextTriggerAt)
        } else {
            planRepo.setWaterReminderStartEpochMillis(0L)
            planRepo.setWaterReminderActive(false)
        }
    }

    private fun showNotification(context: Context, planRepo: PlanRepository) {
        val channelId = "water_reminder"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "饮水提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "到时间喝一口水"
            nm.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val goalLiters = planRepo.dailyWaterGoalMl / 1000
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("该喝水了")
            .setContentText("喝一口 +250 ml，目标 ${goalLiters} L")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            nm.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // 用户未授权 POST_NOTIFICATIONS，静默失败
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}