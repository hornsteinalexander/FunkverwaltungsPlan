package com.funkmonitor.app

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.funkmonitor.app.model.RadioBand
import com.funkmonitor.app.scheduler.RadioScheduler
import com.funkmonitor.app.ui.DashboardScreen
import com.funkmonitor.app.ui.buildSettingsIntent
import com.funkmonitor.app.ui.theme.FunkMonitorTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Alle bereits aktiven Zeitpläne beim Start sicherstellen
        RadioScheduler.rescheduleAll(this)

        setContent {
            FunkMonitorTheme {
                val context = LocalContext.current

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { /* Ergebnis wird beim nächsten Poll automatisch berücksichtigt */ }

                LaunchedEffect(Unit) {
                    val perms = mutableListOf(
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        perms += Manifest.permission.BLUETOOTH_CONNECT
                        perms += Manifest.permission.BLUETOOTH_SCAN
                    }
                    perms += Manifest.permission.READ_PHONE_STATE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        perms += Manifest.permission.POST_NOTIFICATIONS
                    }
                    permissionLauncher.launch(perms.toTypedArray())

                    // Exakte Alarme (Android 12+): ohne diese Erlaubnis feuert der Zeitplan
                    // nur ungefähr statt exakt.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        if (!am.canScheduleExactAlarms()) {
                            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                        }
                    }
                }

                var isRooted by remember { mutableStateOf(false) }
                var liveStatus by remember { mutableStateOf(emptyMap<RadioBand, RadioStatus>()) }

                LaunchedEffect(Unit) {
                    isRooted = RootManager.isDeviceRooted()
                    while (true) {
                        liveStatus = mapOf(
                            RadioBand.WLAN to StatusReaders.wifiStatus(context),
                            RadioBand.BLUETOOTH to StatusReaders.bluetoothStatus(context),
                            RadioBand.MOBILFUNK to StatusReaders.mobileStatus(context),
                            RadioBand.STANDORT to StatusReaders.locationStatus(context)
                        )
                        delay(5000)
                    }
                }

                DashboardScreen(
                    context = context,
                    isRooted = isRooted,
                    liveStatus = liveStatus,
                    onOpenSettings = { bandKey ->
                        try {
                            startActivity(buildSettingsIntent(bandKey))
                        } catch (e: Exception) {
                            startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    }
                )
            }
        }
    }
}
