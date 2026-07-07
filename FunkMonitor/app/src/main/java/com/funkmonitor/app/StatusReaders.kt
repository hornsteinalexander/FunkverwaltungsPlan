package com.funkmonitor.app

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

data class RadioStatus(
    val isOn: Boolean?,       // null = unbekannt/keine Berechtigung
    val detail: String
)

object StatusReaders {

    fun wifiStatus(context: Context): RadioStatus {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val enabled = wm.isWifiEnabled
        val detail = if (enabled && hasFineLocation(context)) {
            val rssi = wm.connectionInfo?.rssi ?: -100
            val level = WifiManager.calculateSignalLevel(rssi, 5) // 0-4
            context.getString(R.string.wifi_signal, level * 25, rssi)
        } else if (enabled) {
            context.getString(R.string.wifi_on_no_perm)
        } else {
            context.getString(R.string.wifi_off)
        }
        return RadioStatus(enabled, detail)
    }

    fun bluetoothStatus(context: Context): RadioStatus {
        if (!hasBtConnect(context)) {
            return RadioStatus(null, context.getString(R.string.bt_permission_missing))
        }
        val bm = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bm.adapter
        val on = adapter?.isEnabled ?: false
        return RadioStatus(on, context.getString(if (on) R.string.bt_on else R.string.bt_off))
    }

    fun mobileStatus(context: Context): RadioStatus {
        if (!hasReadPhoneState(context)) {
            return RadioStatus(null, context.getString(R.string.mobile_permission_missing))
        }
        return try {
            val tm = context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val dataEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) tm.isDataEnabled else true
            val strength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tm.signalStrength?.level ?: -1 // 0 (schwach) - 4 (stark)
            } else -1
            val strengthText = if (strength in 0..4) context.getString(R.string.mobile_signal_suffix, strength * 25) else ""
            val base = context.getString(if (dataEnabled) R.string.mobile_data_on else R.string.mobile_data_off)
            RadioStatus(dataEnabled, base + strengthText)
        } catch (e: SecurityException) {
            RadioStatus(null, context.getString(R.string.mobile_permission_missing))
        }
    }

    fun locationStatus(context: Context): RadioStatus {
        val lm = context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val on = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return RadioStatus(on, context.getString(if (on) R.string.location_on else R.string.location_off))
    }

    private fun hasFineLocation(context: Context) =
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasBtConnect(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasReadPhoneState(context: Context) =
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
}
