package com.example.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.model.EventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Local Event Reminder Receiver & Alarm Scheduler
 * Operates 100% offline without requiring internet connection.
 */
class LocalEventReminderReceiver : BroadcastReceiver() {

  companion object {
    const val CHANNEL_ID = "event_reminders_channel"
    const val CHANNEL_NAME = "Event Reminders"
    const val EXTRA_EVENT_ID = "extra_event_id"
    const val EXTRA_EVENT_TITLE = "extra_event_title"
    const val EXTRA_EVENT_LOCATION = "extra_event_location"
    const val EXTRA_EVENT_TIME = "extra_event_time"

    const val ACTION_TASK_REMINDER = "com.example.ACTION_TASK_REMINDER"
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_TASK_EVENT_TITLE = "extra_task_event_title"

    fun createNotificationChannel(context: Context) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
          CHANNEL_ID,
          CHANNEL_NAME,
          NotificationManager.IMPORTANCE_HIGH
        ).apply {
          description = "Reminders for upcoming events and tasks"
          enableVibration(true)
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
      }
    }

    /**
     * Schedules a local notification reminder for a task/timeline item.
     */
    fun scheduleTaskReminder(
      context: Context,
      taskId: Long,
      taskTitle: String,
      eventTitle: String = "ইভেন্ট",
      triggerTimeMillis: Long
    ) {
      try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LocalEventReminderReceiver::class.java).apply {
          action = ACTION_TASK_REMINDER
          putExtra(EXTRA_TASK_ID, taskId)
          putExtra(EXTRA_TASK_TITLE, taskTitle)
          putExtra(EXTRA_TASK_EVENT_TITLE, eventTitle)
        }

        val reqCode = (100_000 + (taskId % 800_000)).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
          context,
          reqCode,
          intent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = System.currentTimeMillis()
        val trigger = if (triggerTimeMillis <= now) now + 10_000L else triggerTimeMillis

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pendingIntent)
        } else {
          alarmManager.set(AlarmManager.RTC_WAKEUP, trigger, pendingIntent)
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    /**
     * Schedules a local notification 1 day before the event date at 9:00 AM (or 2 hours before if less than a day).
     */
    fun scheduleEventReminder(context: Context, event: EventEntity) {
      try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LocalEventReminderReceiver::class.java).apply {
          action = "com.example.ACTION_EVENT_REMINDER"
          putExtra(EXTRA_EVENT_ID, event.id)
          putExtra(EXTRA_EVENT_TITLE, event.title)
          putExtra(EXTRA_EVENT_LOCATION, event.location)
          putExtra(EXTRA_EVENT_TIME, event.timeFormatted)
        }

        val pendingIntent = PendingIntent.getBroadcast(
          context,
          event.id.toInt(),
          intent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Target: 1 day before the event at 9:00 AM
        val eventCal = Calendar.getInstance().apply {
          timeInMillis = event.dateTimeMillis
        }
        val reminderCal = Calendar.getInstance().apply {
          timeInMillis = event.dateTimeMillis
          add(Calendar.DAY_OF_YEAR, -1)
          set(Calendar.HOUR_OF_DAY, 9)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
        }

        val now = System.currentTimeMillis()
        var triggerTime = reminderCal.timeInMillis

        // If 1 day before at 9 AM already passed, check if event is today
        if (triggerTime <= now) {
          // If event is in the future, trigger 2 hours prior or immediately
          if (event.dateTimeMillis > now + TimeUnit.HOURS.toMillis(2)) {
            triggerTime = event.dateTimeMillis - TimeUnit.HOURS.toMillis(2)
          } else if (event.dateTimeMillis > now) {
            triggerTime = now + 10_000L // 10 seconds for imminent event
          } else {
            return // Past event, do not schedule
          }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
          alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    /**
     * Cancel an event reminder
     */
    fun cancelEventReminder(context: Context, eventId: Long) {
      try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LocalEventReminderReceiver::class.java).apply {
          action = "com.example.ACTION_EVENT_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
          context,
          eventId.toInt(),
          intent,
          PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
          alarmManager.cancel(pendingIntent)
          pendingIntent.cancel()
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    /**
     * Reschedules reminders for all active events (e.g. after phone reboot)
     */
    fun rescheduleAllReminders(context: Context) {
      CoroutineScope(Dispatchers.IO).launch {
        try {
          val db = AppDatabase.getDatabase(context, this)
          val events = db.eventDao().getAllEventsList()
          val now = System.currentTimeMillis()
          for (e in events) {
            if (e.dateTimeMillis > now) {
              scheduleEventReminder(context, e)
            }
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.MY_PACKAGE_REPLACED") {
      rescheduleAllReminders(context)
      return
    }

    createNotificationChannel(context)

    if (intent.action == ACTION_TASK_REMINDER) {
      val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
      val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
      val eventTitle = intent.getStringExtra(EXTRA_TASK_EVENT_TITLE) ?: "ইভেন্ট"

      val openAppIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      }
      val reqCode = (100_000 + (taskId % 800_000)).toInt()
      val contentPendingIntent = PendingIntent.getActivity(
        context,
        reqCode,
        openAppIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val title = "📌 কাজ/টাইমলাইন রিমাইন্ডার"
      val body = "আজকে আপনার কাজ: '$taskTitle' ($eventTitle)"

      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(contentPendingIntent)
        .build()

      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.notify(reqCode, notification)
      return
    }

    val eventId = intent.getLongExtra(EXTRA_EVENT_ID, 0L)
    val eventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: "Upcoming Event"
    val eventLocation = intent.getStringExtra(EXTRA_EVENT_LOCATION) ?: ""
    val eventTime = intent.getStringExtra(EXTRA_EVENT_TIME) ?: ""

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      eventId.toInt(),
      openAppIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val locationText = if (eventLocation.isNotBlank()) " @ $eventLocation" else ""
    val body = "কালকে আপনার '$eventTitle' অনুষ্ঠান$locationText, চেকলিস্টের কাজগুলো শেষ হয়েছে তো?"

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("🔔 ইভেন্ট রিমাইন্ডার (আগামীকাল)")
      .setContentText(body)
      .setStyle(NotificationCompat.BigTextStyle().bigText(body))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(contentPendingIntent)
      .build()

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(eventId.toInt(), notification)
  }
}
