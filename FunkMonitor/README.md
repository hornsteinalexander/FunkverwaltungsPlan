# Funk-Monitor (Android, Kotlin)

Native Android-App mit Root-gestütztem Zeitplan zum automatischen Ein-/Ausschalten
von WLAN, Bluetooth, Mobilfunk und Standort – plus Live-Statusanzeige (echte Werte,
keine Schätzung).

## Öffnen & bauen

1. Android Studio (aktuelle Version) installieren.
2. **File → Open** → diesen `FunkMonitor`-Ordner auswählen.
3. Gradle-Sync abwarten (lädt Abhängigkeiten automatisch herunter).
4. Eigenes Launcher-Icon einfügen: Rechtsklick auf `app` → **New → Image Asset**
   (die mitgelieferte Platzhalter-Grafik ist nur ein einfarbiges Rechteck).
5. **Run ▶** auf einem echten Gerät (Emulator kann kein Root/Bluetooth testen).

## Root-Funktion testen

- Gerät muss bereits mit Magisk (oder vergleichbar) gerootet sein.
- Beim ersten automatischen Schalt-Vorgang erscheint die reguläre
  Root-Bestätigung deines Root-Managers – einmal erlauben, danach läuft es automatisch.
- Ohne Root funktioniert die App weiterhin: Live-Status wird angezeigt, der Zeitplan
  zeigt eine Erinnerung + öffnet die passende Systemeinstellung zum manuellen Schalten.

## Projektstruktur

```
app/src/main/java/com/funkmonitor/app/
├── MainActivity.kt              Einstiegspunkt, Berechtigungen, Live-Polling
├── RootManager.kt                Root-Erkennung + su-Befehlsausführung
├── StatusReaders.kt              Liest echten WLAN/BT/Mobilfunk/Standort-Status
├── model/BandSchedule.kt         Datenmodell + Root-Befehle je Funkart
├── data/ScheduleRepository.kt    Persistenz (SharedPreferences)
├── scheduler/RadioScheduler.kt   Berechnet & setzt Alarme
├── scheduler/ScheduleReceiver.kt Führt den Root-Befehl beim Alarm aus
├── scheduler/BootReceiver.kt     Stellt Zeitpläne nach Neustart wieder her
└── ui/DashboardScreen.kt         Compose-UI (Dashboard + Zeitplan-Editor)
```

## Bekannte Grenzen (technisch, nicht durch Code lösbar)

- Ohne Root gibt es auf Android **keine** Möglichkeit, Funkmodule automatisch zu
  schalten – das ist eine Plattform-Einschränkung seit Android 10 (WLAN) bzw.
  Android 13 (Bluetooth).
- Flugmodus lässt sich auch mit Root nur eingeschränkt zuverlässig über alle
  Hersteller hinweg schalten (Broadcast-Verhalten unterscheidet sich je nach
  Custom-ROM/OEM).
- Root-Erkennung ist heuristisch (Pfad-Check + `which su`) – auf stark
  versteckten Root-Setups (z. B. mit Magisk Hide/Zygisk gegenüber dieser App)
  kann sie fälschlich "nicht gerootet" melden.

Siehe `PLAYSTORE_CHECKLIST.md` für die Veröffentlichung im Play Store.
