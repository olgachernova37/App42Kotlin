# Smart Auto Companion App

A modern Android application prototype designed for automotive manufacturing companies. This app serves as a smart vehicle companion that displays real-time car status, health telemetry, and security parameters in an intuitive, animated interface.

---

## Features

- Multi-Vehicle Support: Seamlessly switch between different company vehicle models.
- Vehicle Telemetry and Diagnostics:
    - Security Status: Central lock, doors, windows, and headlights state.
    - Engine Health: Check Engine light status, engine oil level percentage.
    - Tire Management: Live tire pressure monitoring in bar.
    - Range and Fuel: Fuel or battery percentage and estimated distance to empty in km.
- Randomized Telemetry Generator: Simulates live sensor polling with min and max boundaries for demo purposes.
- Localization (i18n): Full support for three languages:
    - English
    - Czech
    - German
- Fluid UI and Animations: Smooth transition animations when selecting a vehicle to present detailed status panels.

---

## Architecture and Tech Stack

This project follows modern Android development best practices and recommended architecture:

- Language: Kotlin
- UI Framework: Jetpack Compose (Declarative UI)
- Architecture Pattern: MVVM (Model-View-ViewModel)
- Asynchronous Programming: Kotlin Coroutines and StateFlow
- Design System: Material Design 3

com.example.carapp/
├── data/                  // Data layer
│   ├── model/             // Data classes (Car, CarStatus, Enums)
│   └── repository/        // Telemetry generator and Mock data
│
├── ui/                    // UI layer (Jetpack Compose)
│   ├── components/        // Reusable UI components (Metric Cards, Buttons)
│   ├── theme/             // Color schemes, typography, shapes
│   ├── CarViewModel.kt    // UI State management
│   └── HomeScreen.kt      // Main screen and Detail view
│
└── util/                  // Utility classes (Locale/Language Manager)

---

## Getting Started

### Prerequisites
- Android Studio
- JDK: 17 or higher
- Minimum SDK: API 24 (Android 7.0)

### Installation
1. Clone this repository:
   git clone https://github.com/your-username/smart-auto-app.git

2. Open the project in Android Studio.
3. Allow Gradle to sync dependencies.
4. Run the app on an Android Emulator or a physical device.

---

## Screenshots

| Car Selection | Detailed Vehicle Status | Language Switch |
| :---: | :---: | :---: |
| [Image Placeholder] | [Image Placeholder] | [Image Placeholder] |

---

## License

This project was developed as a pet project for educational purposes. Feel free to use it as a reference for Android development with Kotlin and Jetpack Compose.