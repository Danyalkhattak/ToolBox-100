# Contributing to ToolBox-100

Thank you for considering contributing to ToolBox-100! This document provides guidelines and instructions for contributing.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Adding a New Tool](#adding-a-new-tool)
- [Coding Standards](#coding-standards)
- [Submitting Changes](#submitting-changes)
- [Reporting Bugs](#reporting-bugs)

## 🤝 Code of Conduct

This project adheres to a code of conduct that all contributors are expected to follow. Be respectful, inclusive, and constructive in all interactions.

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **SDK**: Android SDK 35 (Android 15)
- **Kotlin**: 2.0.21 or higher
- **Gradle**: 8.7 or higher
- **Device/Emulator**: API 26+ (Android 8.0 Oreo or higher)

### Forking & Cloning

```bash
# 1. Fork the repository on GitHub
# 2. Clone your fork
git clone https://github.com/YOUR_USERNAME/ToolBox-100.git

# 3. Add upstream remote
cd ToolBox-100
git remote add upstream https://github.com/Danyalkhattak/ToolBox-100.git

# 4. Create your feature branch
git checkout -b feature/your-feature-name
```

## 🛠️ Development Setup

1. **Open in Android Studio**
   ```bash
   # Open the project
   open .  # macOS
   # Or: studio . # Linux with Android Studio in PATH
   ```

2. **Sync Gradle**
   - Let Android Studio sync the project automatically
   - Wait for dependencies to download

3. **Build the Project**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run on Device/Emulator**
   - Connect device or start emulator
   - Click Run in Android Studio (Shift+F10)

## 📁 Project Structure

```
app/src/main/java/com/dannyk/toolbox/
├── ui/
│   ├── components/          # Shared UI components
│   ├── navigation/         # Navigation setup
│   ├── screens/            # All screens
│   │   ├── HomeScreen.kt   # Main screen
│   │   └── settings/       # Settings
│   └── theme/              # Theme configuration
├── data/                   # Data layer
├── domain/                 # Domain models
└── tools/                  # Tool registry
```

## ➕ Adding a New Tool

ToolBox-100 is designed to make adding new tools straightforward:

### Step 1: Create the Screen File

Create a new file in the appropriate category folder:
```kotlin
// app/src/main/java/com/dannyk/toolbox/ui/screens/tools/[category]/YourNewToolScreen.kt

package com.dannyk.toolbox.ui.screens.tools.[category]

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopAppBar

@Composable
fun YourNewToolScreen(navController: NavHostController) {
    // Use ToolScreenLayout for consistent UI
    com.dannyk.toolbox.ui.components.ToolScreenLayout(
        title = "Your Tool Name",
        navController = navController
    ) {
        // Your tool's UI here
    }
}
```

### Step 2: Register the Tool

Add your tool to `ToolRegistry.kt`:

```kotlin
// In ToolRegistry.allTools list
Tool(
    id = NEXT_AVAILABLE_ID,
    name = "Your Tool Name",
    description = "Brief description",
    category = Category.YOUR_CATEGORY,
    iconResName = "icon_name",
    route = "tool/your_tool_route"
),
```

### Step 3: Add Navigation Route

In `Navigation.kt`:
```kotlin
composable("tool/your_tool_route") { 
    YourNewToolScreen(navController = navController) 
}
```

### Step 4: Add Icon Mapping (if needed)

In `CommonComponents.kt`'s `getToolIcon()` function:
```kotlin
"icon_name" -> Icons.Outlined.YourIcon,
```

## 📏 Coding Standards

### Kotlin Style

- Follow [Kotlin official coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable/function names
- Prefer `val` over `var`
- Use Compose state management correctly (`remember`, `mutableStateOf`)

### Compose Best Practices

✅ **Do:**
- Keep composables small and focused
- Use `remember` for expensive calculations
- Follow state hoisting principles
- Use proper modifiers order (size → layout → draw)
- Handle configuration changes properly

❌ **Don't:**
- Nest scrollable containers (LazyColumn inside LazyColumn)
- Use `weight()` modifier (use internal API alternatives)
- Forget to handle empty states
- Block the main thread with heavy operations

### Example Good Pattern

```kotlin
@Composable
fun MyToolScreen(navController: NavHostController) {
    var input by remember { mutableStateOf("") }
    
    ToolScreenLayout(
        title = "My Tool",
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input field
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Enter value") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Result display
            if (input.isNotEmpty()) {
                ResultCard(result = processInput(input))
            }
        }
    }
}
```

## 📤 Submitting Changes

### Commit Messages

Follow conventional commits format:

```
feat: add unit converter tool
fix: resolve crash in password generator
docs: update README with new features
style: format code according to style guide
refactor: simplify home screen layout
test: add tests for math utilities
chore: update dependencies
```

### Pull Request Process

1. **Update your branch**
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Test thoroughly**
   - Build debug APK: `./gradlew assembleDebug`
   - Test on device/emulator
   - Verify no new warnings/errors

3. **Create Pull Request**
   - Provide clear title and description
   - Reference any related issues
   - Include screenshots for UI changes
   - List breaking changes (if any)

4. **Code Review**
   - Address review comments promptly
   - Keep discussions professional
   - Ask questions if unclear

## 🐛 Reporting Bugs

Before reporting a bug:

1. **Check existing issues** - Search for duplicates
2. **Gather information**:
   - Device model and Android version
   - App version
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots/logcat if relevant

3. **Create issue** with template:
   ```markdown
   ## Bug Description
   Clear description of the bug
   
   ## Steps to Reproduce
   1. Go to '...'
   2. Click on '...'
   3. Scroll down to '...'
   
   ## Expected Behavior
   What should happen
   
   ## Actual Behavior
   What actually happens
   
   ## Environment
   - Device: [e.g., Pixel 7]
   - Android Version: [e.g., 14]
   - App Version: [e.g., 1.0.0]
   
   ## Screenshots
   If applicable
   ```

## 💡 Feature Requests

We welcome feature requests! Please:

1. Check if the feature already exists or is planned
2. Explain the use case clearly
3. Consider if it fits the project scope
4. Provide mockups if applicable

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/Danyalkhattak/ToolBox-100/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Danyalkhattak/ToolBox-100/discussions)
- **Email**: danny@example.com

## 🙏 Recognition

Contributors will be recognized in:
- README.md Contributors section
- Release notes
- App about screen (for significant contributions)

---

Thank you for contributing to ToolBox-100! 🎉
