# Fertige APK bekommen – ganz ohne Android Studio

Ich kann die App in meiner Umgebung nicht selbst kompilieren (kein Zugriff auf
Googles Android-Build-Server von hier aus). Mit GitHub Actions bekommst du
aber kostenlos eine fertig gebaute, installierbare APK – die Cloud übernimmt
das Bauen für dich.

## Einmalig einrichten (5 Minuten)

1. **GitHub-Konto erstellen** (falls noch nicht vorhanden): https://github.com/signup
2. **Neues Repository anlegen**: oben rechts **+** → **New repository** →
   Name z. B. `funk-monitor` → **Private** oder **Public** (beides geht) →
   **Create repository**
3. Den entpackten `FunkMonitor`-Ordner **komplett** in das leere Repository
   hochladen:
   - Auf der Repo-Seite auf **"uploading an existing file"** klicken
   - Den kompletten Inhalt des `FunkMonitor`-Ordners (inkl. der versteckten
     `.github`-Mappe!) per Drag & Drop hineinziehen
   - Achtung: manche Browser blenden `.github` als "verstecktes" Verzeichnis
     aus – falls es beim Hochladen fehlt, in den Datei-Explorer-Einstellungen
     "versteckte Dateien anzeigen" aktivieren und erneut ziehen
   - Unten auf **Commit changes** klicken

## APK bauen lassen

1. Sobald der Upload fertig ist, oben im Repository auf den Reiter
   **Actions** klicken
2. Der Workflow **"Build APK"** startet automatisch (dauert ca. 3–6 Minuten)
   – ein gelber Punkt bedeutet "läuft", ein grüner Haken "fertig"
3. Auf den fertigen Workflow-Lauf klicken → ganz unten im Abschnitt
   **Artifacts** liegt **FunkMonitor-debug-apk** → anklicken zum Herunterladen
4. Das heruntergeladene ZIP enthält `app-debug.apk`

## APK auf dem Handy installieren

1. Die `app-debug.apk` per Kabel, Cloud-Speicher oder E-Mail aufs Handy bringen
2. Beim Öffnen fragt Android nach Erlaubnis, **"Unbekannte Quellen"**/
   **"Apps aus dieser Quelle installieren"** zuzulassen – bestätigen
3. Installieren, fertig

## Wichtig zu wissen

- Das ist eine **Debug-APK** – perfekt zum Testen auf deinem eigenen Handy,
  aber **nicht** das, was du bei Google Play hochlädst. Für den Play Store
  brauchst du eine **signierte Release-APK/AAB** mit deinem eigenen
  Signierschlüssel (Android Studio: **Build → Generate Signed Bundle/APK**)
  – das ist bewusst ein manueller Schritt, den nur du mit deinem eigenen,
  geheimen Schlüssel machen kannst/sollst.
- Änderst du später Code, reicht es, die geänderten Dateien erneut ins
  GitHub-Repository hochzuladen (oder mit `git push`, falls du Git nutzt) –
  der Workflow baut automatisch eine neue APK.
