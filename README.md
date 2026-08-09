# 🔧 ToolBox-100

<p align="center">
  <strong>100+ Essential Tools in One Beautiful Android App</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Version-1.0.0-blue.svg" alt="Version" />
  <img src="https://img.shields.io/badge/API-26%2B-green.svg" alt="Min SDK" />
  <img src="https://img.shields.io/badge/Target-35-brightgreen.svg" alt="Target SDK" />
  <img src="https://img.shields.io/badge/Kotlin-2.0-purple.svg" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Compose-BOM%2024.09-blue.svg" alt="Compose" />
  <img src="https://img.shields.io/badge/License-MIT-orange.svg" alt="License" />
</p>

---

## ✨ Features

ToolBox-100 is a comprehensive utility app that packs **100+ essential tools** into one beautifully designed, offline-capable Android application. Built with modern Jetpack Compose and Material 3 design.

### 📱 Tool Categories

| Category | Tools | Description |
|----------|-------|-------------|
| 🧮 **Calculators** | 10 | Basic, Scientific, BMI, Tip, Age, Date/Time & more |
| 🔄 **Converters** | 10 | Length, Weight, Temperature, Area, Speed, Data & more |
| ➕ **Math** | 10 | Fractions, GCD/LCM, Prime Checker, Factorial, Power & more |
| 📝 **Text** | 10 | Word/Character Counter, Case Converter, Slug Generator & more |
| 💻 **Developer** | 10 | JSON Formatter, Base64, URL Encoder, Regex Tester & more |
| 🔒 **Security** | 10 | Password Generator, Hash Tools (MD5, SHA), Token Gen & more |
| 🌐 **Internet** | 10 | IP Lookup, DNS, Ping, QR Codes, WiFi QR & more |
| 🎨 **Image & Color** | 10 | Color Picker, Palette Generator, Image Resizer & more |
| 📁 **Files** | 10 | PDF Converter, Hash Checker, File Info & more |
| ⏰ **Everyday** | 10 | Stopwatch, Timer, Pomodoro, World Clock, Notes & more |

### 🎯 Key Highlights

- **🚀 Blazing Fast**: Built entirely with Jetpack Compose for smooth 60fps animations
- **💾 Offline First**: All tools work without internet connection
- **🎨 Beautiful UI**: Material 3 design with dynamic theming (Light/Dark/System)
- **⭐ Smart Favorites**: Bookmark your most-used tools for quick access
- **🕐 Recent History**: Automatically tracks recently used tools
- **🔍 Powerful Search**: Instantly find any tool by name or description
- **📊 Category Filtering**: Filter tools by category with horizontal chip selection
- **🔒 Privacy Focused**: No data collection, no ads, fully open source

---

## 📸 Screenshots

<!-- Add screenshots here when available -->
<p align="center">
  <!-- <img src="screenshots/home.png" width="200" alt="Home Screen" /> -->
  <!-- <img src="screenshots/tools.png" width="200" alt="Tools List" /> -->
  <!-- <img src="screenshots/settings.png" width="200" alt="Settings" /> -->
</p>
*Screenshots coming soon*

---

## 🛠️ Tech Stack

- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose BOM 2024.09
- **Architecture**: MVVM + Clean Architecture
- **Material Design**: Material 3 (You)
- **Navigation**: Compose Navigation
- **Data Storage**: DataStore Preferences
- **Dependency Injection**: Manual DI (Kotlin)
- **Build System**: Gradle with AGP 8.7
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 35 (Android 15)

---

## 📦 Installation

### From Source

```bash
# Clone the repository
git clone https://github.com/Danyalkhattak/ToolBox-100.git

# Open in Android Studio
# Build and run on device/emulator
```

### APK Releases

Download the latest APK from the [Releases](https://github.com/Danyalkhattak/ToolBox-100/releases) page.

---

## 🏗️ Project Structure

```
app/src/main/java/com/dannyk/toolbox/
├── ui/
│   ├── components/          # Reusable UI components
│   │   └── CommonComponents.kt
│   ├── navigation/         # Navigation graph
│   │   └── Navigation.kt
│   ├── screens/
│   │   ├── HomeScreen.kt    # Main home screen
│   │   ├── settings/        # Settings screen
│   │   └── tools/           # Tool screens by category
│   │       ├── calculator/
│   │       ├── converters/
│   │       ├── math/
│   │       ├── text/
│   │       ├── developer/
│   │       ├── security/
│   │       ├── internet/
│   │       ├── image/
│   │       ├── files/
│   │       └── everyday/
│   └── theme/              # App theme configuration
├── data/
│   └── local/preferences/ # DataStore preferences
├── domain/
│   └── model/             # Domain models
├── tools/
│   └── ToolRegistry.kt     # Tool registry & search
└── ToolBoxApplication.kt  # Application class
```

---

## 🤝 Contributing

Contributions are welcome! Please read our [CONTRIBUTING.md](CONTRIBUTING.md) guide.

### Quick Start

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-tool`)
3. Commit your changes (`git commit -m 'Add amazing tool'`)
4. Push to the branch (`git push origin feature/amazing-tool`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Danny K** - [GitHub](https://github.com/Danyalkhattak)

---

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Material Design 3](https://m3.material.io/) - Design system
- [AndroidX Libraries](https://developer.android.com/jetpack/androidx) - Core libraries
- [Kotlin](https://kotlinlang.org/) - Programming language
- All open-source libraries used in this project

---

## ⭐ Star History

[![Star History Chart](https://api.star-history.com/svg?repos=Danyalkhattak/ToolBox-100&type=Date)](https://star-history.com/#Danyalkhattak/ToolBox-100&Date)

---

<p align="center">
  <strong>If you find this project useful, please give it a star ⭐</strong>
</p>
