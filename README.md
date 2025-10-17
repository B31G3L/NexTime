# NexTime ⏰

Eine moderne Android-App zum Erstellen und Verwalten von Countdowns für wichtige Ereignisse. Mit flexiblen Anzeigeformaten, Benachrichtigungen, Widgets und vielem mehr.

## 🌟 Features

### ✅ Bereits implementiert

- **Countdown-Verwaltung**: Erstelle, bearbeite und lösche Countdowns
- **7 Anzeigeformate**: 
  - FULL_DETAILED (Jahre, Monate, Tage + HH:MM:SS)
  - DAYS_ONLY (nur Tage)
  - DAYS_HOURS (Tage + Stunden/Minuten)
  - HOURS_MINUTES (nur Stunden/Minuten)
  - FULL_TIME (Tage + HH:MM:SS)
  - WEEKS_DAYS (Wochen + Tage)
  - MONTHS_DAYS (Monate + Tage)
- **Flexible Zeiteinstellungen**: Mit oder ohne Uhrzeit
- **Farbige Gestaltung**: 10 vordefinierte Farben + Custom Hex-Farben
- **Live-Updates**: Sekundengenau aktualisierte Countdowns
- **Detailansicht**: Umfangreiche Statistiken und Informationen
- **Swipe-Gesten**: Wischen zum Bearbeiten (rechts) oder Löschen (links)
- **Dark Mode**: Vollständige Unterstützung (System/Hell/Dunkel)
- **Haptisches Feedback**: Verschiedene Vibrationsmuster
- **Teilen-Funktion**: Countdowns mit anderen teilen
- **Benachrichtigungen**: Flexible Erinnerungen zu konfigurierbaren Zeitpunkten
- **14 Erinnerungsoptionen**: Von 5 Minuten bis 1 Monat vorher
- **Widget für Homescreen**: 2x2 Widget mit Live-Updates
- **Count-up Modus**: Automatische Erkennung vergangener Daten
- **Empty State**: Schöne Willkommensansicht mit Vorschlägen
- **Test-Daten**: 10 Beispiel-Countdowns beim Start
- **Thema-Preferences**: Standard-Uhrzeit konfigurierbar

### ⏳ In Entwicklung / Geplant

- [x] **Erweiterte Widgets**: Größere Widget-Varianten (2x4, 4x4)
- [ ] **Kategorien/Tags**: Countdowns gruppieren und filtern
- [ ] **Suche & Filter**: Nach Titel, Datum oder Status suchen
- [ ] **Statistiken & Analytics**: Wieviele Countdowns, abgelaufen, etc.
- [ ] **Wiederkehrende Countdowns**: Jährliche Ereignisse automatisieren
- [ ] **Favoriten**: Wichtige Countdowns markieren
- [ ] **Sortierung**: Nach Datum, Titel oder Kategorie
- [ ] **Export/Import**: CSV/JSON Export und Import
- [x] **Cloud-Synchronisation**: iCloud oder Google Drive Sync
    -   nicht notwenig!!!
- [x] **Mehrsprachigkeit**: English, Français, Español, etc.
- [ ] **Responsive UI**: Bessere Anpassung an größere Bildschirme
- [ ] **Material You**: Dynamic Color Support (Android 12+)
- [ ] **Accessibility**: Verbesserte Screen-Reader-Unterstützung
- [ ] **Watchface-Support**: Countdowns auf der Smartwatch
- [ ] **Shortcuts**: App Shortcuts für schnelle Aktionen
- [ ] **Batch-Operationen**: Mehrere Countdowns gleichzeitig löschen
- [ ] **Rich Notifications**: Mit Bildern und Aktionen
- [ ] **Hintergrund-Musik**: Alarmsound bei Ablauf konfigurieren
- [ ] **Datum-Picker Verbesserung**: Year-Picker für schnellere Auswahl
- [ ] **Vorlage-System**: Countdown-Vorlagen speichern und laden

## 📱 Screenshots

*Coming Soon - Bitte folgende Ansichten dokumentieren:*
- [ ] Hauptbildschirm mit Countdown-Liste
- [ ] Add/Edit Dialog mit allen Optionen
- [ ] Detail-Ansicht eines Countdowns
- [ ] Empty State Willkommensansicht
- [ ] Widget auf dem Homescreen
- [ ] Settings Dialog
- [ ] Dark Mode Vergleich

## 🛠️ Technologie-Stack

| Bereich | Technologie | Version |
|---------|-------------|---------|
| **Sprache** | Kotlin | 1.9.20 |
| **UI Framework** | Jetpack Compose | 2023.10.01 |
| **Material Design** | Material 3 | Latest |
| **Architektur** | MVVM + Repository | - |
| **Datenbank** | Room | 2.6.1 |
| **Einstellungen** | DataStore Preferences | 1.0.0 |
| **Async** | Coroutines | 1.7.3 |
| **Task Scheduling** | WorkManager | 2.9.0 |
| **Build System** | Gradle KTS | 8.2+ |
| **Min SDK** | Android 8.0 | 26 |
| **Target SDK** | Android 15 | 35 |
| **Compile SDK** | Android 15 | 36 |

## 📊 Projektstruktur

```
app/src/main/java/de/beigel/nextime/
│
├── MainActivity.kt                  # App-Einstiegspunkt
│
├── data/
│   ├── database/
│   │   └── database.kt              # Room DB, DAOs, Converter
│   ├── model/
│   │   └── Models.kt                # Datenmodelle, Enums, Hilfsfunktionen
│   └── repository/
│       └── CountdownRepository.kt    # Repository Pattern
│
├── ui/
│   ├── components/
│   │   ├── AboutDialog.kt           # Info & Support Dialog
│   │   ├── AddEditCountdownDialog.kt # Erstellen/Bearbeiten UI
│   │   ├── CountdownCard.kt         # Countdown-Kartendarstellung
│   │   ├── EmptyStateView.kt        # Leerzustand UI
│   │   ├── SimpleFab.kt             # Floating Action Button
│   │   └── SwipeableCountdownCard.kt # Swipe-Gesten
│   │
│   ├── screens/
│   │   ├── MainScreen.kt            # Haupt-Bildschirm
│   │   ├── CountdownDetailScreen.kt # Detail-Ansicht
│   │   └── AddEditCountdownScreen.kt # Add/Edit Screen
│   │
│   ├── theme/
│   │   ├── Theme.kt                 # Material 3 Farben & Typo
│   │   ├── ThemeManager.kt          # DataStore Preferences
│   │   └── DesignSystem.kt          # Zentrales Design System
│   │
│   └── viewmodel/
│       └── CountdownViewModel.kt    # MVVM ViewModel
│
├── notifications/
│   ├── CountdownNotificationManager.kt  # Benachrichtigungslogik
│   ├── NotificationReceiver.kt          # BroadcastReceiver
│   └── NotificationScheduler.kt         # AlarmManager Integration
│
├── widget/
│   ├── CountdownWidget.kt           # Widget Provider
│   ├── CountdownWidgetConfigActivity.kt # Widget Config UI
│   └── WidgetUpdateWorker.kt        # WorkManager für Updates
│
└── utils/
    └── HapticFeedback.kt            # Vibrationsmuster
```

## 🚀 Installation & Setup

### Voraussetzungen

- **Android Studio**: Hedgehog (2023.1.1) oder neuer
- **JDK**: Version 17 oder höher
- **Android SDK**: Level 36 (Android 15)
- **Gradle**: 8.2 oder höher
- **RAM**: Mindestens 8GB empfohlen

### Schnellstart

```bash
# Repository klonen
git clone https://github.com/deinusername/nextime.git
cd nextime

# Android Studio öffnen
open -a "Android Studio" .

# Oder direkt bauen
./gradlew build

# Auf Gerät/Emulator ausführen
./gradlew installDebug
```

### Build-Varianten

```bash
# Debug Build
./gradlew assembleDebug

# Release Build
./gradlew assembleRelease

# Tests ausführen
./gradlew test
./gradlew connectedAndroidTest
```

## 💡 Verwendung

### Countdown erstellen

1. Tippe auf **+** Button rechts unten
2. Gib einen **Titel** ein (z.B. "Geburtstag")
3. Wähle ein **Datum** aus dem Kalender
4. Optional: Aktiviere **Uhrzeit** und wähle eine Zeit
5. Wähle ein **Anzeigeformat** (empfohlen: FULL_DETAILED)
6. Wähle eine **Farbe** (10 Vorlagen + Custom Hex)
7. Optional: Aktiviere **Benachrichtigungen** und wähle **Erinnerungen**
8. Tippe **SPEICHERN**

### Countdown bearbeiten

- **Wische rechts** auf der Karte, oder
- Tippe auf die **Karte** → Tippe **Bearbeiten-Icon**

### Countdown löschen

- **Wische links** auf der Karte, oder
- Öffne **Detail-Ansicht** → Tippe **Löschen-Icon**

### Detail-Ansicht

- Tippe auf eine **Countdown-Karte**
- Zeigt:
  - Großes Countdown-Display
  - Startdatum/-zeit
  - Gesamtdauer und verbleibende Zeit
  - Erstellungsdatum
  - Teilen & Bearbeiten & Löschen Optionen

### Widget einrichten

1. Lange auf Homescreen drücken
2. **Widgets** auswählen
3. **NexTime Widget** auswählen
4. Widget platzieren
5. **Countdown auswählen** das angezeigt werden soll
6. ✅ Fertig - Widget aktualisiert sich automatisch

## ⚙️ Konfiguration

### Einstellungen (via Settings Icon oben rechts)

#### Design
- **Systemeinstellung**: Folgt dem Gerät-Setting
- **Hell**: Erzwingt helles Theme
- **Dunkel**: Erzwingt dunkles Theme

#### Standard-Uhrzeit
- Zeit wählen die verwendet wird, wenn keine Uhrzeit angegeben ist
- Default: 00:00

### Benachrichtigungen

Pro Countdown konfigurierbar:
- ✅ Ein/Aus Toggle
- 14 verschiedene Zeitpunkte (von 5 Min bis 1 Monat vorher)
- Zum Zeitpunkt des Ablaufs (AT_TIME)

## 🔍 Analyse der Implementierung

### ✅ Stärken

| Aspekt | Status | Notizen |
|--------|--------|---------|
| **Architektur** | ✅ Sehr gut | MVVM, Repository Pattern, separation of concerns |
| **UI/UX** | ✅ Sehr gut | Material 3, responsive, swipe gestures, haptic feedback |
| **Performance** | ✅ Gut | Live updates mit Coroutines, effiziente DB-Abfragen |
| **Code-Qualität** | ✅ Gut | Gut strukturiert, Kotlin best practices |
| **Datenbank** | ✅ Gut | Room mit Type Converters für LocalDateTime |
| **Notifications** | ✅ Gut | AlarmManager + BroadcastReceiver |
| **Widget** | ✅ Gut | Remote Views, Config Activity, Live Updates |
| **Dokumentation** | ⚠️ Mittel | README vorhanden, aber Code-Comments fehlen teilweise |

### ⚠️ Verbesserungspotenzial

| Bereich | Problem | Lösung |
|---------|---------|--------|
| **Error Handling** | Minimal | Try-Catch Blöcke erweitern, Fehler-Logging |
| **Unit Tests** | Nicht vorhanden | Tests für ViewModel, Repository, Models |
| **Logging** | Nicht vorhanden | Timber oder Firebase Crashlytics |
| **Accessibility** | Minimal | ContentDescriptions, Screen Reader Support |
| **Performance** | Gute DB, aber keine Pagination | Pagination für große Listen implementieren |
| **Security** | N/A | DataStore ist sicher, aber Backup Rules prüfen |
| **Codekommentare** | Teilweise | Wichtige Funktionen kommentieren |

## 📈 Roadmap & Zukünftige Features

### 🔴 Phase 1: Basis-Verbesserungen (1-2 Wochen)

- [ ] Code-Dokumentation verbessern
- [ ] Unit Tests für Models schreiben
- [ ] Error Handling erweitern
- [ ] Logging implementieren (Timber)
- [ ] Content Descriptions für Accessibility

### 🟡 Phase 2: Neue Features (2-4 Wochen)

- [ ] **Kategorien/Tags**: Countdowns gruppieren
- [ ] **Suche**: Nach Titel oder Status suchen
- [ ] **Sortierung**: Nach Datum, Titel, Kategorie
- [ ] **Favoriten**: Wichtige Countdowns markieren
- [ ] **Statistiken**: Dashboard mit Übersichtsdaten

### 🟢 Phase 3: Erweiterte Features (4-8 Wochen)

- [ ] **Wiederkehrende Countdowns**: Jährliche Events
- [ ] **Export/Import**: CSV/JSON Support
- [ ] **Mehrsprachigkeit**: i18n Support (EN, FR, ES, DE)
- [ ] **Dynamic Colors**: Material You auf Android 12+
- [ ] **Batch-Operationen**: Mehrere löschen gleichzeitig

### 🔵 Phase 4: Premium Features (8+ Wochen)

- [ ] **Cloud Sync**: Google Drive / Firebase
- [ ] **Watch OS**: Smartwatch Companion App
- [ ] **Wearable Widgets**: Watch Faces
- [ ] **Rich Notifications**: Mit Bildern & Aktionen
- [ ] **Custom Alarm Sounds**: Verschiedene Ringtone Optionen
- [ ] **Vorlage-System**: Vordefinierte Countdowns
- [ ] **Advanced Analytics**: Statistiken & Trends

## 🐛 Bekannte Probleme & Limitierungen

### Aktuell bekannt

- **Widget-Refresh**: Bei vielen Widgets kann es zu Verzögerungen kommen
  - *Lösung in Planung*: WorkManager Optimization

- **Test-Daten**: Werden bei jedem Start neu eingefügt
  - *Lösung*: Migration zu echter Versionskontrolle

- **Große Datumssprünge**: Bei Datumsangaben 100+ Jahre in die Zukunft möglich
  - *Lösung*: Validierung bei Eingabe hinzufügen

### Technische Limitierungen

- Min API Level 26 (Android 8.0) - Manche Features nur ab API 31+
- Widget-Updates alle 1-15 Minuten (Android Limitation)
- Notifications können von Benutzer deaktiviert werden

## 📦 Dependencies

```kotlin
// Core Android
androidx.core:core-ktx:1.12.0
androidx.lifecycle:lifecycle-runtime-ktx:2.7.0
androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0
androidx.activity:activity-compose:1.8.2

// Compose UI
androidx.compose:compose-bom:2023.10.01
androidx.compose.ui:ui
androidx.compose.ui:ui-graphics
androidx.compose.material3:material3
androidx.compose.material:material-icons-extended

// Room Database
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1
androidx.room:room-compiler:2.6.1 (ksp)

// DataStore
androidx.datastore:datastore-preferences:1.0.0

// WorkManager
androidx.work:work-runtime-ktx:2.9.0

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3

// Testing
junit:junit:4.13.2
androidx.test.ext:junit:1.1.5
androidx.test.espresso:espresso-core:3.5.1
androidx.compose.ui:ui-test-junit4
```

## 🧪 Testing

### Unit Tests

```bash
./gradlew test
```

### Android Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

### Aktueller Status
- ⚠️ Nur Template-Tests vorhanden
- 🔄 Tests für folgende Klassen geplant:
  - [ ] CountdownViewModel
  - [ ] CountdownRepository
  - [ ] Models & Extensions
  - [ ] NotificationScheduler

## 🔒 Sicherheit

- ✅ Daten lokal in Room Database gespeichert
- ✅ DataStore für sichere Preferences
- ✅ Keine Internetverbindung notwendig
- ⚠️ Backup: Kann über Android Backup aktiviert werden
- 📋 Berechtigungen:
  - `VIBRATE`: Für haptisches Feedback
  - `POST_NOTIFICATIONS`: Für Benachrichtigungen
  - `SCHEDULE_EXACT_ALARM`: Für exakte Alarme
  - `USE_EXACT_ALARM`: Für Benachrichtigungen

## 📄 Lizenz

Dieses Projekt steht unter der **MIT-Lizenz** - siehe [LICENSE](LICENSE) Datei für Details.

```
MIT License

Copyright (c) 2025 Beigel

Permission is hereby granted, free of charge...
```

## 👤 Autor

**Beigel** - Leidenschaftlicher Solo-Entwickler

- 💼 Contact: beigel.dev@gmail.com
- ☕ Support: [Ko-fi](https://ko-fi.com/beigel)

## 🙏 Danksagungen

- 🎨 **Material Design 3** von Google
- 🚀 **Jetpack Compose Team** für das Framework
- 🔧 **Kotlin Team** für die Sprache
- 📚 **Android Community** für Support und Inspiration

## 💬 Support & Kontakt

### Feedback & Feature-Anfragen

Öffne ein [GitHub Issue](https://github.com/deinusername/nextime/issues) mit:
- Deine Idee oder Feedback
- Screenshots falls relevant
- Android Version & Gerät

### Bug-Bericht

In der App:
- ℹ️ Icon → Support → "Fehler melden"
- Vorausgefüllte E-Mail mit System-Infos

### Direkter Kontakt

- 📧 E-Mail: beigel.dev@gmail.com
- ☕ Ko-fi: Supportiere die Entwicklung

## 🌟 Unterstütze das Projekt

Falls dir NexTime gefällt:

- ⭐ Gib dem Repository einen **Star**
- 📤 Teile die App mit Freunden
- 💡 Schlag Features vor
- 🐛 Berichte Bugs
- ☕ Unterstütze auf [Ko-fi](https://ko-fi.com/beigel)
- ⭐ Bewerte die App im Play Store

---

### Version History

| Version | Datum | Highlights |
|---------|-------|-----------|
| 1.0.2 | 17.10.2025 | Multi Language |
| 1.0.1 | 17.10.2025 | Beta Release: Widget, Notifications, 7 Display Formats |
| 1.0.0 | TBD | Initial Release (geplant) |

**Zuletzt aktualisiert**: 17. Oktober 2025

⏰ **"Zeit ist kostbar - behalte sie im Blick"** ⏰