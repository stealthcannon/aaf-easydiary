package me.blog.korn123.easydiary.helper

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.PowerManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.simplemobiletools.commons.helpers.isOreoPlus
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.models.Alarm

open class BaseAlarmWorkExecutor(val context: Context) {

    fun openNotification(alarm: Alarm) {
        context.run {
            EasyDiaryDbHelper.insertActionLog(ActionLog("BaseAlarmWorkExecutor", "openNotification", "label", alarm.label), this)
            val pendingIntent = getOpenAlarmTabIntent(alarm)
            val notification = getAlarmNotification(pendingIntent, alarm)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(alarm.id, notification)

            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (isScreenOn()) {
                scheduleNextAlarm(alarm, true)
            } else {
                scheduleNextAlarm(alarm, false)
                powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, "myApp:notificationLock").apply {
                    acquire(3000)
                }
            }
        }
    }

    @SuppressLint("NewApi")
    fun openSnoozeNotification(alarm: Alarm) {
        context.run {
            val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            if (isOreoPlus()) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel("${NOTIFICATION_CHANNEL_ID}_alarm", "${NOTIFICATION_CHANNEL_NAME}_alarm", importance)
                channel.description = NOTIFICATION_CHANNEL_DESCRIPTION
                notificationManager.createNotificationChannel(channel)
            }

            val title = "Fail!!!"
            val message = "The device entered doze mode and the backup operation using the network failed. If you touch the notification window, the backup operation will resume."
            val builder = NotificationCompat.Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_alarm")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_easydiary)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_googledrive_upload))
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message).setSummaryText(title))
                    .setContentIntent(
                            PendingIntent.getActivity(this, 0, Intent(this, DiaryMainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                putExtra(DOZE_SCHEDULE, true)
                                putExtra(SettingsScheduleFragment.ALARM_ID, alarm.id)
                            }, PendingIntent.FLAG_UPDATE_CURRENT)
                    )
            notificationManager.notify(alarm.id, builder.build())
        }
    }

    open fun executeWork(alarm: Alarm) {
        context.run {
            when (alarm.workMode) {
                Alarm.WORK_MODE_DIARY_BACKUP_LOCAL -> {
                    exportRealmFile()
                    openNotification(alarm)
                }
                Alarm.WORK_MODE_DIARY_WRITING -> openNotification(alarm)
            }
        }
    }
}