package com.funkmonitor.app.ui

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.funkmonitor.app.R
import com.funkmonitor.app.RadioStatus
import com.funkmonitor.app.data.ScheduleRepository
import com.funkmonitor.app.model.BandSchedule
import com.funkmonitor.app.model.RadioBand
import com.funkmonitor.app.scheduler.RadioScheduler
import com.funkmonitor.app.ui.theme.*

@Composable
fun DashboardScreen(
    context: Context,
    isRooted: Boolean,
    liveStatus: Map<RadioBand, RadioStatus>,
    onOpenSettings: (String) -> Unit
) {
    var schedules by remember { mutableStateOf(ScheduleRepository.loadAll(context)) }

    fun persist(updated: BandSchedule) {
        schedules = schedules.map { if (it.bandKey == updated.bandKey) updated else it }
        ScheduleRepository.update(context, updated)
        RadioScheduler.rescheduleAll(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.dashboard_title),
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.dashboard_subtitle),
                        color = Muted,
                        fontSize = 13.sp
                    )
                }
                LanguageSwitcher()
            }
        }

        item { RootStatusCard(isRooted) }

        item {
            Text(stringResource(R.string.live_status_heading), color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        items(RadioBand.entries) { band ->
            val status = liveStatus[band]
            LiveStatusCard(band = band, status = status, onOpenSettings = onOpenSettings)
        }

        item {
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.schedule_heading), color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            if (!isRooted) {
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.schedule_no_root_hint),
                    color = Muted,
                    fontSize = 11.5.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = {
                    schedules = schedules.map {
                        val updated = it.copy(enabled = true, startTime = "20:00", endTime = "08:30", days = List(7) { true })
                        ScheduleRepository.update(context, updated)
                        updated
                    }
                    RadioScheduler.rescheduleAll(context)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Panel, contentColor = Amber)
            ) {
                Text(stringResource(R.string.schedule_preset_button))
            }
        }

        items(schedules) { sch ->
            val band = RadioBand.fromKey(sch.bandKey)
            ScheduleCard(band = band, schedule = sch, onChange = { persist(it) })
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun LanguageSwitcher() {
    val currentTag = AppCompatDelegate.getApplicationLocales().toLanguageTags().lowercase()
    val isEnglish = currentTag.startsWith("en")

    Row(
        modifier = Modifier
            .background(Panel, RoundedCornerShape(8.dp))
            .padding(3.dp)
    ) {
        LangChip(stringResource(R.string.lang_toggle_de), active = !isEnglish) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("de"))
        }
        LangChip(stringResource(R.string.lang_toggle_en), active = isEnglish) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
        }
    }
}

@Composable
private fun LangChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (active) AmberDim else Color.Transparent, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(label, color = if (active) Amber else Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RootStatusCard(isRooted: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(if (isRooted) Ok else Danger, RoundedCornerShape(50))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(if (isRooted) R.string.root_detected_title else R.string.root_not_detected_title),
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(if (isRooted) R.string.root_detected_body else R.string.root_not_detected_body),
                color = Muted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun LiveStatusCard(band: RadioBand, status: RadioStatus?, onOpenSettings: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(band.labelRes), color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                val dotColor = when (status?.isOn) {
                    true -> Ok
                    false -> Danger
                    else -> Muted
                }
                Box(Modifier.size(8.dp).background(dotColor, RoundedCornerShape(50)))
            }
            Spacer(Modifier.height(6.dp))
            Text(status?.detail ?: stringResource(R.string.status_loading), color = Muted, fontSize = 12.5.sp)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { onOpenSettings(band.key) }, contentPadding = PaddingValues(0.dp)) {
                Text(stringResource(R.string.open_settings_link), color = Cyan, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ScheduleCard(band: RadioBand, schedule: BandSchedule, onChange: (BandSchedule) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val dayLabels = listOf(
        stringResource(R.string.day_sun), stringResource(R.string.day_mon), stringResource(R.string.day_tue),
        stringResource(R.string.day_wed), stringResource(R.string.day_thu), stringResource(R.string.day_fri),
        stringResource(R.string.day_sat)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(band.labelRes), color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Switch(
                    checked = schedule.enabled,
                    onCheckedChange = { onChange(schedule.copy(enabled = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Amber, checkedTrackColor = AmberDim)
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TimeField(
                    label = stringResource(R.string.schedule_from_off),
                    time = schedule.startTime,
                    onPick = { onChange(schedule.copy(startTime = it)) },
                    context = context,
                    modifier = Modifier.weight(1f)
                )
                TimeField(
                    label = stringResource(R.string.schedule_to_on),
                    time = schedule.endTime,
                    onPick = { onChange(schedule.copy(endTime = it)) },
                    context = context,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                dayLabels.forEachIndexed { i, label ->
                    val active = schedule.days[i]
                    Box(
                        modifier = Modifier
                            .background(if (active) AmberDim else PanelBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                val newDays = schedule.days.toMutableList()
                                newDays[i] = !newDays[i]
                                onChange(schedule.copy(days = newDays))
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(label, color = if (active) Amber else Muted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeField(
    label: String,
    time: String,
    onPick: (String) -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(label, color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(3.dp))
        val parts = time.split(":").map { it.toInt() }
        OutlinedButton(
            onClick = {
                TimePickerDialog(
                    context,
                    { _, hour, minute -> onPick("%02d:%02d".format(hour, minute)) },
                    parts[0], parts[1], true
                ).show()
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
        ) {
            Text(time)
        }
    }
}

fun buildSettingsIntent(bandKey: String): Intent = when (bandKey) {
    "bluetooth" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
    "wlan" -> Intent(Settings.ACTION_WIFI_SETTINGS)
    "standort" -> Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    else -> Intent(Settings.ACTION_WIRELESS_SETTINGS)
}
