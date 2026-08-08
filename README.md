# ToolBox-100

A production-ready Android application with 100 fully functional utility tools.

## Features

- **100 Tools** across 10 categories
- **Material Design 3** with dark/light/system theme support
- **Offline-first** design - most tools work without internet
- **Jetpack Compose** + **MVVM Architecture**
- **Kotlin** with modern Android development practices

## Tool Categories

### Calculators (1-10)
Basic Calculator, Scientific Calculator, Percentage Calculator, Discount Calculator, Tip Calculator, Split Bill Calculator, Age Calculator, Date Difference Calculator, Time Difference Calculator, BMI Calculator

### Converters (11-20)
Length Converter, Weight Converter, Temperature Converter, Area Converter, Volume Converter, Speed Converter, Time Converter, Data Storage Converter, Number System Converter, Roman Numeral Converter

### Math (21-30)
Fraction Calculator, Ratio Calculator, Proportion Calculator, Average Calculator, GCD & LCM, Prime Number Checker, Factorial Calculator, Power Calculator, Square Root Calculator, Random Number Generator

### Text (31-40)
Word Counter, Character Counter, Sentence Counter, Case Converter, Text Reverser, Remove Duplicate Lines, Sort Lines, Find & Replace, Text Diff, Slug Generator

### Developer (41-50)
JSON Formatter, JSON Validator, Base64 Encoder/Decoder, URL Encoder/Decoder, HTML Entity Encoder/Decoder, UUID Generator, Regex Tester

### Security (51-60)
Password Generator, Password Strength Checker, SHA-256 Hash, SHA-1 Hash, MD5 Hash, HMAC Generator, Random Token Generator, PIN Generator, Passphrase Generator, Hash Identifier

### Internet (61-70)
My IP Address, DNS Lookup, Ping Tester, HTTP Status Checker, User-Agent Viewer, URL Parser, QR Code Generator, QR Code Scanner, Wi-Fi QR Generator, Barcode Scanner

### Images & Colors (71-80)
Image Compressor, Image Resizer, Image Cropper, Image Format Converter, Color Picker, HEX to RGB Converter, RGB to HSL Converter, Color Palette Generator, Contrast Checker, Image Metadata Viewer

### Files (81-90)
Image to PDF, PDF to Images, File Size Converter, File Hash Checker, Text to PDF, Text to QR Code, QR Code to Text, File Extension Info, MIME Type Lookup, File Name Generator

### Everyday (91-100)
Stopwatch, Countdown Timer, Pomodoro Timer, World Clock, Metronome, Coin Flip, Dice Roller, Random Choice Picker, Habit Counter, Notes

## Requirements

- Android Studio Hedgehog or newer
- Android SDK 34+
- JDK 17+
- Gradle 8.5+

## Build Instructions

### 1. Clone and Setup

```bash
git clone https://github.com/Danyalkhattak/ToolBox-100.git
cd ToolBox-100
```

### 2. Configure SDK Path

Copy the template and set your SDK path:

```bash
cp local.properties.template local.properties
# Edit local.properties and set sdk.dir to your Android SDK path
```

### 3. Build Debug APK

```bash
./gradlew assembleDebug
```

The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### 4. Build Release APK

```bash
./gradlew assembleRelease
```

The APK will be at: `app/build/outputs/apk/release/app-release.apk`

## Project Structure

```
app/src/main/java/com/dannyk/toolbox/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room database
│   │   ├── NoteDao.kt              # Notes DAO
│   │   ├── entity/                 # Database entities
│   │   └── preferences/            # DataStore preferences
├── domain/model/                   # Data models
├── tools/
│   └── ToolRegistry.kt             # All 100 tool definitions
├── ui/
│   ├── components/                 # Reusable UI components
│   ├── screens/
│   │   ├── HomeScreen.kt           # Main home screen
│   │   ├── SettingsScreen.kt       # Settings screen
│   │   └── tools/                  # All 100 tool screens
│   │       ├── calculator/         # Tools 1-10
│   │       ├── converters/         # Tools 11-20
│   │       ├── math/               # Tools 21-30
│   │       ├── text/               # Tools 31-40
│   │       ├── developer/          # Tools 41-50
│   │       ├── security/           # Tools 51-60
│   │       ├── internet/           # Tools 61-70
│   │       ├── image/              # Tools 71-80
│   │       ├── files/              # Tools 81-90
│   │       └── everyday/           # Tools 91-100
│   ├── theme/                      # Material 3 theming
│   └── navigation/                 # Navigation graph
├── updates/                        # GitHub update checker
├── MainActivity.kt                 # Main activity
└── ToolBoxApplication.kt           # Application class
```

## Key Dependencies

- Jetpack Compose BOM 2024.01.00
- Material 3
- Navigation Compose 2.7.6
- Room 2.6.1
- DataStore Preferences 1.0.0
- OkHttp 4.12.0
- ZXing 4.3.0 (QR/Barcode)
- Coil 2.5.0 (Image loading)

## Features Implemented

### UI/UX
- Material 3 design system
- System/Light/Dark theme support
- Responsive layouts
- Smooth animations
- Accessible components

### Core Functionality
- Search across all tools
- Favorites with local persistence
- Recently used tracking
- Category browsing
- Copy/share functionality

### Settings
- Theme selection
- Clear history
- Version info
- GitHub update checking via Releases API

### Offline Support
- All calculators work offline
- All converters work offline
- All math tools work offline
- All text tools work offline
- All security tools work offline
- Most developer tools work offline

## License

MIT License - See LICENSE file for details

## Contributing

Contributions are welcome! Please read the contributing guidelines before submitting PRs.
