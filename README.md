# Brightness Bar

Brightness Bar is an Android application developed in Kotlin that allows users to control screen brightness by swiping horizontally across the system status bar (notification bar).

## Features

- Status Bar Gestures: Change brightness level with a simple swipe on the top of the screen.
- AMOLED Dark Theme: Pure black interface (#000000) that automatically follows system dark mode settings.
- Quick Settings Tile: Toggle the application functionality directly from the notification panel.
- State Synchronization: Real-time synchronization between the application interface and the QS Tile.
- Automatic Brightness Preservation: Manual adjustments do not disable the system's automatic brightness mode.

## Permissions Required

To function correctly, the application requires two special permissions:

1. Modify System Settings: Needed to change the actual brightness level of the display.
2. Accessibility Service: Needed to detect touch gestures on the system status bar area.

Note: The Accessibility Service is used exclusively to capture swipe coordinates and does not collect or transmit any user data.

## Installation

1. Clone the repository.
2. Build the project using Android Studio or Gradle.
3. Install the APK on your device.
4. Grant the necessary permissions through the initial setup screen.

## Technical Details

- Language: Kotlin
- UI Framework: Jetpack Compose
- Target SDK: 36
- Minimum SDK: 24
