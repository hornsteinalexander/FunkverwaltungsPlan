package com.funkmonitor.app.data

import android.content.Context
import com.funkmonitor.app.model.BandSchedule
import com.funkmonitor.app.model.RadioBand
import org.json.JSONArray
import org.json.JSONObject

object ScheduleRepository {
    private const val PREFS = "funk_monitor_schedules"
    private const val KEY = "schedules_json"

    fun loadAll(context: Context): List<BandSchedule> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, null)
        val stored = if (raw != null) parse(raw) else emptyList()
        val byKey = stored.associateBy { it.bandKey }
        // Stellt sicher, dass für jede bekannte Funkart ein Eintrag existiert,
        // auch wenn die App aktualisiert wurde und neue Bänder hinzukamen.
        return RadioBand.entries.map { byKey[it.key] ?: BandSchedule(bandKey = it.key) }
    }

    fun save(context: Context, schedules: List<BandSchedule>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY, serialize(schedules)).apply()
    }

    fun update(context: Context, updated: BandSchedule) {
        val all = loadAll(context).map { if (it.bandKey == updated.bandKey) updated else it }
        save(context, all)
    }

    private fun serialize(schedules: List<BandSchedule>): String {
        val arr = JSONArray()
        schedules.forEach { s ->
            val obj = JSONObject()
            obj.put("bandKey", s.bandKey)
            obj.put("enabled", s.enabled)
            obj.put("startTime", s.startTime)
            obj.put("endTime", s.endTime)
            obj.put("days", JSONArray(s.days))
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parse(json: String): List<BandSchedule> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val daysArr = obj.getJSONArray("days")
                val days = (0 until daysArr.length()).map { daysArr.getBoolean(it) }
                BandSchedule(
                    bandKey = obj.getString("bandKey"),
                    enabled = obj.getBoolean("enabled"),
                    startTime = obj.getString("startTime"),
                    endTime = obj.getString("endTime"),
                    days = days
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
