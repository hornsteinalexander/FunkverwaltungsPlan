package com.funkmonitor.app.scheduler

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.funkmonitor.app.R
import com.funkmonitor.app.RootManager
import com.funkmonitor.app.data.ScheduleRepository
import com.funkmonitor.app.model.RadioBand
import com.funkmonitor.app.model.RadioCommands

class ScheduleReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_BAND = "band"
        const val EXTRA_EDGE = "edge"
        private const val CHANNEL_ID = "funk_monitor_schedule"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val bandKey = intent.getStringExtra(EXTRA_BAND) ?: return
        val edge = intent.getStringExtra(EXTRA_EDGE) ?: return
        val band = RadioBand.fromKey(bandKey)

        val commands = if (edge == RadioScheduler.EDGE_OFF) RadioCommands.off(band) else RadioCommands.on(band)

        val success = if (RootManager.isDeviceRooted()) {
            RootManager.execRootCommands(commands)
        } else {
            false
        }

        notifyResult(context, context.getString(band.labelRes), edge, success)

        // Nächstes Vorkommen für dieselbe Kante erneut einplanen
        val schedules = ScheduleRepository.loadAll(context)
        schedules.firstOrNull { it.bandKey == bandKey }?.let { sch ->
            if (sch.enabled) RadioScheduler.scheduleNext(context, sch, edge)
        }
    }

    private fun notifyResult(context: Context, bandLabel: String, edge: String, success: Boolean) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, context.getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_DEFAULT
            )
            nm.createNotificationChannel(channel)
        }

        val actionText = context.getString(
            if (edge == RadioScheduler.EDGE_OFF) R.string.notif_action_off else R.string.notif_action_on
        )
        val body = if (success) {
            context.getString(R.string.notif_success, bandLabel, actionText)
        } else {
            context.getString(R.string.notif_failure, bandLabel, actionText)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_title))
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            nm.notify(bandLabel.hashCode(), notification)
        }
    }
}
