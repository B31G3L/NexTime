# NexTime ⏰

Eine moderne Android-App zum Erstellen und Verwalten von Countdowns für wichtige Ereignisse.

## Features ✨

- **Countdown-Verwaltung**: Erstelle, bearbeite und lösche Countdowns
- **Flexible Zeiteinstellungen**: Mit oder ohne Uhrzeit
- **Nächte-Zähler**: Optional werden die Nächte bis zum Ereignis angezeigt
- **Farbige Gestaltung**: 10 vordefinierte Farben zur individuellen Anpassung
- **Vorlagen**: Schnelle Erstellung mit vordefinierten Vorlagen (Geburtstag, Urlaub, Prüfung, etc.)
- **Live-Updates**: Countdowns mit Uhrzeit werden sekundengenau aktualisiert
- **Fortschrittsanzeige**: Visueller Fortschrittsbalken mit Prozentanzeige
- **Detailansicht**: Umfangreiche Statistiken und Informationen zu jedem Countdown
- **Swipe-Gesten**: Wischen zum Bearbeiten (rechts) oder Löschen (links)
- **Dark Mode**: Vollständige Unterstützung für helles und dunkles Design
- **Haptisches Feedback**: Intuitive Vibrationen bei Interaktionen
- **Teilen-Funktion**: Countdowns einfach mit anderen teilen

## Screenshots 📱

*Coming Soon*

## Technologie-Stack 🛠️

- **Sprache**: Kotlin
- **UI Framework**: Jetpack Compose mit Material 3
- **Architektur**: MVVM (Model-View-ViewModel)
- **Datenbank**: Room Database
- **Einstellungen**: DataStore Preferences
- **Dependency Injection**: Manuelle Dependency Injection
- **Coroutines**: Für asynchrone Operationen
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)

## Projektstruktur 📂

```
app/src/main/java/de/beigel/nextime/
├── data/
│   ├── database/
│   │   └── database.kt          # Room-Datenbank, DAOs & Converter
│   ├── model/
│   │   └── Models.kt             # Datenmodelle & Hilfsfunktionen
│   └── repository/
│       └── CountdownRepository.kt # Repository-Pattern
├── ui/
│   ├── components/
│   │   ├── AddEditCountdownDialog.kt
│   │   ├── CountdownCard.kt
│   │   ├── CountdownTemplate.kt
│   │   ├── EmptyStateView.kt
│   │   └── SwipeableCountdownCard.kt
│   ├── screens/
│   │   ├── CountdownDetailScreen.kt
│   │   └── MainScreen.kt
│   ├── theme/
│   │   ├── Theme.kt
│   │   └── ThemeManager.kt
│   └── viewmodel/
│       └── CountdownViewModel.kt
├── utils/
│   └── HapticFeedback.kt         # Vibrations-Feedback
└── MainActivity.kt
```

## Installation 🚀

### Voraussetzungen

- Android Studio Hedgehog (2023.1.1) oder neuer
- JDK 17
- Android SDK 35
- Gradle 8.2+

### Build-Schritte

1. Repository klonen:
```bash
git clone https://github.com/deinusername/nextime.git
cd nextime
```

2. Projekt in Android Studio öffnen

3. Gradle Sync durchführen

4. App auf Gerät oder Emulator ausführen

## Verwendung 💡

### Countdown erstellen

1. Tippe auf den **+** Button
2. Wähle eine Vorlage oder erstelle einen eigenen Countdown
3. Fülle Titel und Datum aus
4. Optional: Aktiviere Uhrzeit und/oder Nächte-Anzeige
5. Wähle eine Farbe
6. Speichern

### Countdown bearbeiten

- Wische die Karte nach **rechts** oder
- Tippe auf das **Bearbeiten-Icon** in der Karte

### Countdown löschen

- Wische die Karte nach **links** oder
- Tippe auf das **Löschen-Icon** in der Karte

### Detail-Ansicht

- Tippe auf eine Countdown-Karte für detaillierte Informationen

## Konfiguration ⚙️

### Dark Mode

Einstellungen → Dark Mode Toggle

### Fortschrittsanzeige

Einstellungen → Fortschritt anzeigen

## Dependencies 📦

```kotlin
// Core
androidx.core:core-ktx:1.12.0
androidx.lifecycle:lifecycle-runtime-ktx:2.7.0
androidx.activity:activity-compose:1.8.2

// Compose
androidx.compose:compose-bom:2023.10.01
androidx.compose.material3:material3
androidx.compose.material:material-icons-extended

// Room Database
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1
androidx.room:room-compiler:2.6.1 (ksp)

// DataStore
androidx.datastore:datastore-preferences:1.0.0

// Coroutines
kotlinx-coroutines-android:1.7.3
```

## Roadmap 🗺️

- [ ] Widget für den Homescreen
- [ ] Benachrichtigungen
- [ ] Export/Import von Countdowns
- [ ] Cloud-Synchronisation
- [ ] Benutzerdefinierte Farben (Farbwähler)
- [ ] Mehrsprachigkeit (Englisch, Französisch, Spanisch)
- [ ] Kategorien für Countdowns
- [ ] Wiederkehrende Countdowns
- [ ] Statistiken und Insights

## Bekannte Probleme 🐛

Aktuell keine bekannten Probleme.

## Mitwirken 🤝

Contributions sind willkommen! Bitte erstelle ein Issue oder einen Pull Request.

1. Fork das Projekt
2. Erstelle einen Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Committe deine Änderungen (`git commit -m 'Add some AmazingFeature'`)
4. Push zum Branch (`git push origin feature/AmazingFeature`)
5. Öffne einen Pull Request

## Lizenz 📄

Dieses Projekt steht unter der MIT-Lizenz - siehe [LICENSE](LICENSE) Datei für Details.

## Autor ✍️

**Beigel**

## Danksagungen 🙏

- Material Design 3 von Google
- Jetpack Compose Team
- Alle Contributors und Tester

---

**Hinweis**: Diese App befindet sich in aktiver Entwicklung. Features und UI können sich ändern.

## Support 💬

Bei Fragen oder Problemen öffne bitte ein [Issue](https://github.com/deinusername/nextime/issues).

⭐ Vergiss nicht, dem Projekt einen Star zu geben, wenn es dir gefällt!