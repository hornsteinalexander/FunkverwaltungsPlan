# Play-Store-Checkliste für Funk-Monitor

Diese App darf grundsätzlich bei Google Play veröffentlicht werden – Root-Nutzung
allein ist kein Ablehnungsgrund. Wichtig ist, wie du sie deklarierst.

## 1. Store-Listing – Pflichtangaben

- **Store-Beschreibung**: Explizit erwähnen, dass automatisches Ein-/Ausschalten
  ein **gerootetes Gerät (z. B. mit Magisk)** voraussetzt, und was ohne Root
  passiert (Erinnerung + Einstellungen-Link statt automatischem Schalten).
  Beispieltext:

  > "Für automatisches Ein-/Ausschalten von WLAN, Bluetooth, Mobilfunk und
  > Standort nach Zeitplan ist ein gerootetes Android-Gerät erforderlich
  > (z. B. mit Magisk). Ohne Root zeigt die App weiterhin den Live-Status an
  > und erinnert dich rechtzeitig, öffnet aber nur die passende
  > Systemeinstellung zum manuellen Schalten."

- Grund: Googles Richtlinie zu irreführendem Verhalten verlangt, dass
  Kernfunktionen, die zusätzliche Voraussetzungen haben, klar offengelegt werden.

## 2. Datenschutzerklärung

- Pflicht, weil die App auf Standort-, Bluetooth- und Telefonstatus-Berechtigungen
  zugreift. Nutze die mitgelieferte `PRIVACY_POLICY_TEMPLATE.md`, veröffentliche
  sie unter einer eigenen URL (z. B. GitHub Pages, eigene Domain) und trage die
  URL im Play Console unter **Store-Präsenz → App-Content → Datenschutzerklärung**
  ein.

## 3. App-Content-Fragebogen (Play Console)

- **Data Safety Form**: angeben, dass Standort, Geräte-ID (Telefonstatus) und
  Bluetooth-Gerätestatus gelesen, aber nicht an Dritte weitergegeben oder
  dauerhaft gespeichert werden (nur lokal auf dem Gerät).
- **Berechtigungs-Deklaration** für `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM`:
  Im entsprechenden Formular begründen, dass exakte Alarme der Kernzweck der App
  sind (zeitgenaues Schalten laut Nutzer-Zeitplan).
- **Hinweise an die Prüfer** (App-Content → "Anweisungen für den Zugriff"):
  kurz erklären, dass volle Funktionalität ein gerootetes Testgerät erfordert,
  und dass die App ohne Root ebenfalls nutzbar bleibt (Live-Status + Erinnerungen).

## 4. Technische Vorgaben, die bereits im Code stecken

- Kein Exploit, keine automatische Root-Beschaffung – die App nutzt nur `su`,
  wenn der Nutzer sein Gerät bereits selbst gerootet und der App über seinen
  eigenen Root-Manager (Magisk) Rechte erteilt hat.
- Läuft ohne Absturz auch ohne Root (Fallback: Erinnerung + Settings-Link).
- `targetSdk 35`, `neverForLocation`-Flag für Bluetooth-Scan, damit keine
  unnötige Standortberechtigung vorgetäuscht wird.

## 5. Vor der Einreichung testen

- Auf einem **nicht gerooteten** Gerät durchklicken: App darf nicht abstürzen,
  muss den Root-Hinweis klar zeigen.
- Auf einem **gerooteten** Gerät: kompletten Zeitplan-Durchlauf über Nacht testen.
- Berechtigungsabfragen (Standort, Bluetooth, Telefonstatus, Benachrichtigungen,
  exakte Alarme) müssen alle sauber ablehnbar sein, ohne dass die App abstürzt.

## 6. Alternative zu Google Play

Falls die Review-Reibung zu hoch wird (Root-Apps werden manchmal genauer
geprüft): direkte APK-Verteilung (eigene Website/GitHub Releases) oder
**F-Droid** sind für Root-nahe Nischen-Apps ebenfalls üblich und von deiner
Zielgruppe (die ohnehin schon rootet) meist akzeptiert.
