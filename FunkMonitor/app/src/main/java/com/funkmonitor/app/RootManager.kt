package com.funkmonitor.app

import java.io.DataOutputStream
import java.io.File

/**
 * Prüft Root-Verfügbarkeit und führt Shell-Befehle mit su aus.
 *
 * WICHTIG für Play-Store-Konformität: Diese Klasse verschafft der App KEINEN
 * Root-Zugriff und nutzt keine Exploits. Sie geht davon aus, dass der Nutzer
 * sein Gerät bereits selbst gerootet hat (z. B. mit Magisk) und der App die
 * su-Anfrage über seinen eigenen Root-Manager manuell bestätigt.
 */
object RootManager {

    private val knownSuPaths = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su",
        "/system/bin/.ext/.su"
    )

    fun isDeviceRooted(): Boolean {
        if (knownSuPaths.any { File(it).exists() }) return true
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val result = process.inputStream.bufferedReader().readLine()
            process.destroy()
            !result.isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Führt Befehle mit su aus. Löst bei erstem Aufruf pro Prozesslebensdauer
     * die Root-Bestätigung des Nutzers (z. B. Magisk-Dialog) aus.
     * Gibt true zurück, wenn der su-Prozess ohne Fehler beendet wurde.
     */
    fun execRootCommands(commands: List<String>): Boolean {
        return try {
            val process = ProcessBuilder("su").redirectErrorStream(true).start()
            val out = DataOutputStream(process.outputStream)
            commands.forEach { out.writeBytes("$it\n") }
            out.writeBytes("exit\n")
            out.flush()
            out.close()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}
