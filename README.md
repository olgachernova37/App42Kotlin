App42 — Car Companion

A small Android app that works like a car companion app (similar to MyŠkoda or MyAudi). It shows a list of cars, and for each car it shows its status: lock state, engine health, oil level, tire pressure, range, mileage and the next service.

The data is not real. The app generates random values for each car, but every car has its own realistic range, so the numbers still make sense.

Built with Kotlin and Jetpack Compose as a learning project.

Features
Car list with photos of three cars: Škoda Superb 2018, Audi A6 2018 and Škoda Felicia.
Car detail screen with:
Overview — oil level, range (km), mileage (km), km until next service
Tire pressure — all four wheels (FL, FR, RL, RR), in bar
Status — locked / unlocked, Engine OK / Check Engine
Service condition label — Excellent, Good or Needs attention, calculated from oil level, engine state and tire pressure.
Simulated telemetry — each car has its own value ranges:
Car	Usually shows
Škoda Superb 2018 (newer)	Excellent
Audi A6 2018	Good
Škoda Felicia (old)	Needs attention
Values are generated once when the screen opens, so they don't change on every click.
Search — filter cars by name from the top bar.
Animated transition — the car photo moves and grows from the list to the detail screen (shared element transition).
Light and dark theme — chosen in the app menu and saved, so it stays after the app is closed.
Three languages — English, Czech (čeština) and German (Deutsch). You can switch the language inside the app.
About and Author dialogs in the ⋮ menu.
System Back button / gesture returns from the detail screen to the list.
Tech stack
	
Language	Kotlin
UI	Jetpack Compose, Material 3
Animations	AnimatedContent, SharedTransitionLayout
Language switch	AppCompatDelegate.setApplicationLocales
Theme storage	SharedPreferences
Build	Gradle (Kotlin DSL), version catalog (libs.versions.toml)
Native	CMake + a small C++ JNI library (native-lib.cpp, from the project template)
minSdk 24 (Android 7.0), targetSdk / compileSdk 37
Java 17
Project structure
app/src/main/
├── java/com/olhadem/app42/
│   ├── MainActivity.kt            # Entry point, top bar, search, menu, language dialog
│   ├── data/
│   │   ├── Car.kt                 # Car model (id, name, image)
│   │   ├── CarStatus.kt           # Telemetry model (lock, engine, oil, tires, range…)
│   │   ├── CarRepository.kt       # Car list + random status generator
│   │   └── ServiceCondition.kt    # Excellent / Good / Poor calculation
│   └── ui/
│       ├── CarCard.kt             # Card in the car list
│       ├── CarDetailScreen.kt     # Detail screen with all status sections
│       ├── MoreOptionsButton.kt   # ⋮ menu button
│       └── theme/                 # Colors, ThemeMode, ThemePreferences
├── cpp/                           # Native C++ library (JNI)
└── res/
    ├── values/strings.xml         # English
    ├── values-cs/strings.xml      # Czech
    ├── values-de/strings.xml      # German
    └── drawable/                  # Car images
Getting started
Requirements
Android Studio (recent version that supports AGP 9.x)
JDK 17
Android SDK 37
CMake 3.22.1 and the NDK (Android Studio can install them in SDK Manager → SDK Tools)
Run the app
Clone the repository:
bash
   git clone https://github.com/olgachernova37/App42Kotlin.git
Open the project folder in Android Studio.
Wait for Gradle sync to finish.
Choose an emulator or a connected phone and click Run ▶.

Or build from the command line:

bash
./gradlew assembleDebug

The APK will be in app/build/outputs/apk/debug/.

How the status is calculated

calculateServiceCondition() gives each part a score from 0 to 100:

Oil — the oil level itself (0–100)
Engine — 100 if OK, 20 if "Check Engine"
Tires — 100 minus the average difference from the ideal pressure (2.3 bar)

The average of the three scores decides the label:

Average score	Label
75 or more	Excellent
45 – 74	Good
below 45	Needs attention
Screenshots
Car list	Car details	Settings menu
add screenshot	add screenshot	add screenshot
Author

Olha Demianyk — Master's graduate in Software Engineering and student at 42 Prague.

This is a personal learning project for Android development with Kotlin and Jetpack Compose.
