package com.funkmonitor.app.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.funkmonitor.app.data.ScheduleRepository
import com.funkmonitor.app.model.BandSchedule
import java.util.Calendar

object RadioScheduler {

    const val EDGE_OFF = "OFF"
    const val EDGE_ON = "ON"

    fun rescheduleAll(context: Context) {
        val schedules = ScheduleRepository.loadAll(context)
        schedules.forEach { sch ->
            if (sch.enabled) {
                scheduleNext(context, sch, EDGE_OFF)
                scheduleNext(context, sch, EDGE_ON)
            } else {
                cancel(context, sch, EDGE_OFF)
                cancel(context, sch, EDGE_ON)
            }
        }
    }

    fun scheduleNext(context: Context, sch: BandSchedule, edge: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val timeStr = if (edge == EDGE_OFF) sch.startTime else sch.endTime
        val parts = timeStr.split(":").map { it.toInt() }

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parts[0])
            set(Calendar.MINUTE, parts[1])
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        // Vorwärts zum nächsten aktivierten Wochentag springen (max. 7 Versuche)
        var guard = 0
        while (!sch.days[cal.get(Calendar.DAY_OF_WEEK) - 1] && guard < 8) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            guard++
        }
        if (guard >= 8) return // kein einziger Wochentag aktiviert

        val pi = pendingIntent(context, sch.bandKey, edge)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            }
        } catch (e: SecurityException) {
            // Nutzer hat "Alarme & Erinnerungen" nicht erlaubt -> ungefähre Zeit als Fallback
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }

    fun cancel(context: Context, sch: BandSchedule, edge: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, sch.bandKey, edge))
    }

    private fun pendingIntent(context: Context, bandKey: String, edge: String): PendingIntent {
        val intent = Intent(context, ScheduleReceiver::class.java).apply {
            putExtra(ScheduleReceiver.EXTRA_BAND, bandKey)
            putExtra(ScheduleReceiver.EXTRA_EDGE, edge)
        }
        val requestCode = (bandKey + edge).hashCode()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
