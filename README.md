# PhoneDashboard

> [!WARNING]
> **Work in Progress**: This project is currently under active development. Some parameters (such as default city coordinates, alerts regions, and grid configurations) are hardcoded in `MainActivity` for rapid testing and prototyping. Flexible settings and customization UI are planned for future releases.

**PhoneDashboard** is an open-source Android application designed to repurpose unused or daily-driver Android smartphones and tablets into dedicated, always-on smart desk clocks and interactive dashboards.

Featuring a modular grid layout, real-time weather and air-raid alerts, health tracking via an embedded HTTP REST server, and built-in OLED burn-in protection, PhoneDashboard provides a sleek and functional display for your desk or nightstand.

---

## Key Features

- 📐 **Adaptive N*M Grid Engine**: Dynamically calculates cell dimensions, font scaling, widget layouts for any screen size or orientation.
- 🛡️ **Burn-In Protection**: An integrated `WidgetPixelShifter` subtly translates grid elements using randomized geometric patterns (square, cross, diagonal shifts) to prevent screen burn-in on always-on displays.
- ☀️ **Always-On Immersive Mode**: Forces full-screen execution (`FLAG_KEEP_SCREEN_ON`) with hidden system navigation and status bars.
- 📡 **Embedded Micro REST Server**: Powered by NanoHTTPD, running a background service on port `8080` to receive push data (such as health metrics from iOS Shortcuts or custom webhooks) directly into the app.
- 🧩 **Interactive Built-in Widgets** (that include but are not limited to!):
  - 🕒 **Tall Clock (3×2)**: Bold digital clocks with stroke rendering and tap-to-cycle color palettes.
  - 🌤️ **Weather Widget (2×2)**: Integrates with the free **Open-Meteo API** to display current temperature, daily highs/lows, condition icons, weather-matched background gradients, and precipitation predictions (e.g., *"75% precip. chance in 1.5 hours"*).
  - 🚨 **Air Raid Alerts / Trivoga Widget (1×1)**: Real-time region monitoring powered by `alerts.in.ua` with safe (green), alert (yellow/red), and timestamp indicators.
  - 👟 **Steps & Distance Widget (2×1)**: Interactive pill-shaped progress bar tracking live health metrics. Tap to toggle between step count and distance (meters) modes.
  - 🔋 **Battery Gauge (1×1)**: Live battery percentage, charging state bolt indicator, and color thresholds.

---

## Getting Started

### Prerequisites

- **Android Studio**: Ladybug / Jellyfish or newer recommended.
- **Android SDK**: Minimum API 24 (Android 7.0), Target API 37.
- **JDK**: Java 11 or higher.

### Building & Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/vovan/phonedashboard.git
   cd phonedashboard
   ```

2. **Configure Local Environment**:
   Copy the template property file to create your local setup:
   ```bash
   cp local.properties.example local.properties
   ```
   Set your local Android SDK location in `local.properties`:
   ```properties
   sdk.dir=/path/to/your/android/sdk
   ```

3. **Configure API Keys (Optional)**:
   To enable Ukrainian air raid alert data from `alerts.in.ua`, obtain an API token and add it to `local.properties`:
   ```properties
   ALERTS_IN_UA_KEY=your_alerts_in_ua_api_key_here
   ```
   *(If left empty, the Trivoga widget will gracefully default to an unknown/inactive status state.)*

4. **Build and Run**:
   Build the debug APK using Gradle:
   ```bash
   ./gradlew assembleDebug
   ```
   Or deploy directly via Android Studio to a connected device.

---

## Tech Stack & Architecture

- **Language**: Java 11 (with Core Library Desugaring enabled for modern `java.time` APIs).
- **UI Framework**: Android Views with custom `ViewGroup` layout calculations, custom `Drawable` implementations (`SquircleDrawable`, `PillGradientDrawable`), and custom `TextView` subclasses (`StrokedTextView`, `TallTextView`).
- **Networking**: Retrofit 3.0, OkHttp 5.1, Gson.
- **HTTP Server**: NanoHTTPD 2.3.1.
- **Asynchronous Processing**: Handler / Looper event loops, `CompletableFuture`.

See [`app/README.md`](app/README.md) for detailed developer documentation, widget lifecycle details, and instructions on creating custom widgets.

