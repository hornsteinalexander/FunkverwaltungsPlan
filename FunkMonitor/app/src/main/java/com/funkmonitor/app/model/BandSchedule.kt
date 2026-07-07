package com.funkmonitor.app.model

import com.funkmonitor.app.R

/**
 * Represents the automatic on/off schedule for one radio ("Band").
 *
 * days: index 0 = Sonntag ... 6 = Samstag (entspricht Calendar.DAY_OF_WEEK - 1)
 * startTime: Uhrzeit, zu der die Funkart AUSgeschaltet wird ("HH:mm")
 * endTime:   Uhrzeit, zu der die Funkart wieder EINgeschaltet wird ("HH:mm")
 */
data class BandSchedule(
    val bandKey: String,
    var enabled: Boolean = false,
    var startTime: String = "20:00",
    var endTime: String = "08:30",
    var days: List<Boolean> = List(7) { true }
)

enum class RadioBand(val key: String, val labelRes: Int) {
    MOBILFUNK("mobilfunk", R.string.band_mobilfunk),
    WLAN("wlan", R.string.band_wlan),
    BLUETOOTH("bluetooth", R.string.band_bluetooth),
    STANDORT("standort", R.string.band_standort);

    companion object {
        fun fromKey(key: String) = entries.first { it.key == key }
    }
}

/** Root-Shell-Befehle je Funkart und Richtung. Erfordert su-Zugriff (z. B. via Magisk). */
object RadioCommands {
    fun off(band: RadioBand): List<String> = when (band) {
        RadioBand.MOBILFUNK -> listOf("svc data disable")
        RadioBand.WLAN -> listOf("svc wifi disable")
        RadioBand.BLUETOOTH -> listOf("svc bluetooth disable")
        RadioBand.STANDORT -> listOf("settings put secure location_mode 0")
    }

    fun on(band: RadioBand): List<String> = when (band) {
        RadioBand.MOBILFUNK -> listOf("svc data enable")
        RadioBand.WLAN -> listOf("svc wifi enable")
        RadioBand.BLUETOOTH -> listOf("svc bluetooth enable")
        RadioBand.STANDORT -> listOf("settings put secure location_mode 3")
    }
}
