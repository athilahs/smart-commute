# SmartCommute - London Underground Status & Alerts

<p align="center">
  <img src="googleplay/ic_launcher_512.png" width="128" height="128" alt="SmartCommute Logo">
</p>

<p align="center">
  Real-time London Underground status with smart notifications for your daily commute
</p>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.smartcommute">
    <img src="https://img.shields.io/badge/Google_Play-Download-green?style=for-the-badge&logo=google-play" alt="Get it on Google Play">
  </a>
</p>

---

## 📱 Features

### 🚇 Real-Time Tube Status
- ✅ View current status for all 11 London Underground lines
- 📍 Detailed disruption information including affected stations
- 🚫 Closure and crowding updates
- 🔄 Pull to refresh for latest data
- 📶 Offline-first with local data caching

### 🔔 Smart Status Alerts
- ⏰ Schedule up to 10 alarms for your commute times
- 🎯 Monitor multiple tube lines in a single alarm
- 🔁 Recurring weekly alarms for regular commutes
- 📅 One-time alarms for occasional travel
- 🔇 **Silent notifications** when all lines run smoothly
- 🔊 **Audible alerts** when disruptions are detected
- ⚙️ Easy alarm management and editing

### 🎨 Line Details
- 👆 Tap any line to see comprehensive details
- 🚉 Station-by-station status information
- 📝 Current disruption descriptions
- 📊 Historical status tracking
- 🎨 Line-specific color themes

### ✨ Modern Design
- 🎭 Material Design 3 interface
- 🎬 Smooth animations and transitions
- 👀 Comfortable viewing experience
- 🧭 Intuitive navigation
- 🌙 Dark mode ready (coming soon)

### 🔒 Privacy First
- 🚫 No personal data collection
- 👤 No user accounts required
- 📵 No advertising or tracking
- 💾 All data stays on your device
- 🔓 Open source codebase

---

## 🏗️ Architecture

SmartCommute follows **Clean Architecture** principles with MVVM pattern:

```
app/
├── core/                      # Shared utilities and base classes
├── data/                      # Data layer (repositories, local/remote sources)
├── domain/                    # Business logic (use cases, models)
└── feature/                   # Feature modules
    ├── linestatus/            # Tube status list screen
    ├── linedetails/           # Individual line details
    └── statusalerts/          # Smart alerts and alarms
```

### Tech Stack

- **Language**: Kotlin 2.3.0
- **UI**: Jetpack Compose (BOM 2025.12.01)
- **Design**: Material Design 3
- **DI**: Hilt 2.57.2
- **Database**: Room 2.8.4 (SQLite)
- **Networking**: Retrofit 3.0.0
- **Images**: Glide Compose 1.0.0-beta08
- **Async**: Kotlin Coroutines 1.10.2 + Flow
- **Background**: WorkManager
- **Navigation**: Navigation Compose 2.9.6
- **Build**: Gradle 8.13 with Kotlin DSL
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 36

### Data Flow

1. **UI Layer** (Compose) → ViewModel
2. **ViewModel** → Use Cases (Domain)
3. **Use Cases** → Repositories (Data)
4. **Repositories** → Local DB (Room) + Remote API (Retrofit)
5. **Data flows back** via Kotlin Flow

### Key Components

- **Room Database**: Offline-first caching for line status and details
- **WorkManager**: Scheduled background checks for alarms
- **AlarmManager**: Precise timing for status notifications
- **Repository Pattern**: Single source of truth for data
- **Hilt Modules**: Dependency injection throughout

---

## 📄 API

SmartCommute uses the [Transport for London (TfL) Unified API](https://api.tfl.gov.uk/):

- **Endpoint**: `https://api.tfl.gov.uk/Line/Mode/tube/Status`
- **Rate Limits**: 500 requests per minute (with API key)
- **Documentation**: [TfL API Docs](https://api-portal.tfl.gov.uk/)

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Write clear commits with prefixes (feat:, fix:, improve:)
4. Push to your branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Add comments for complex logic
- Write tests for new features

### Writing Good Commits

Release notes are auto-generated from commits. Use clear prefixes:

```bash
# Features (shows as "New")
feat: Add dark mode support
feature: Add Elizabeth line

# Bug fixes (shows as "Fixed")
fix: Prevent crash on network timeout
bug: Fix alarm not triggering on boot

# Improvements (shows as "Improved")
improve: Reduce app startup time
enhance: Better error messages
optimize: Faster database queries
```

---

## 📱 Supported Lines

All 11 London Underground lines:

- 🔴 **Bakerloo**
- 🔴 **Central**
- 🟡 **Circle**
- 🟢 **District**
- 🟣 **Hammersmith & City**
- ⚫ **Jubilee**
- 🟣 **Metropolitan**
- ⚫ **Northern**
- 🔵 **Piccadilly**
- 🔵 **Victoria**
- 🟢 **Waterloo & City**

---

## 🐛 Known Issues

See [Issues](https://github.com/athilahs/smart-commute/issues) for current bugs and feature requests.

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 📧 Contact

- **Issues**: [GitHub Issues](https://github.com/athilahs/smart-commute/issues)
- **Discussions**: [GitHub Discussions](https://github.com/athilahs/smart-commute/discussions)
- **Email**: athilahsdev@gmail.com

---

## ⭐ Show Your Support

If you find SmartCommute useful, please consider:
- ⭐ Starring this repository
- 📱 Rating the app on Google Play
- 🐛 Reporting bugs
- 💡 Suggesting features
- 🤝 Contributing code

---

<p align="center">
  Made with ❤️ for London commuters
</p>

<p align="center">
  <sub>SmartCommute is not affiliated with Transport for London</sub>
</p>
