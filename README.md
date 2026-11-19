# MonoTodo v4 — Minimalist Monochrome Android To-Do App

Redesigned for a **clean, distraction-free black-to-white UI** and **locked high-FPS smoothness**.

## What Changed in v4
- **Clean Minimalist UI**:
  - Deep obsidian header with date, title, collapsible search bar, and a 3dp animated progress hairline
  - Rounded luminous silver-to-white task sheet below so every card sits cleanly with sharp hairline contrast
  - Minimalist task rows (no cluttered badge pills — just title, optional note, subtle category/due line, and an obsidian dot for High priority)
- **Performance & Bug Fixes**:
  - Moved `SoundPool` WAV preloading and `SharedPreferences` JSON writes off the main UI thread (`Dispatchers.IO`)
  - Removed `composed {}` modifier allocations inside `LazyColumn` items and eliminated per-pixel disk writes while dragging the volume slider
  - Zero infinite animation loops; cached gradient via `drawWithCache`; GitHub Actions builds an **R8-minified Release APK** (`app-release.apk`)
- **Subtle Customizable Sounds**:
  - Tap the top-right **Tune** icon to choose **Soft**, **Crisp**, or **Zen**, adjust volume, toggle per-event audio, or switch haptics.

## Build the `.apk`
- **GitHub Actions**: Push the unzipped folder to your GitHub repo (`main` branch), open **Actions**, and download **`monotodo-release-apk`** (`app-release.apk`).
- **Android Studio**: Open the unzipped folder and build the `release` or `debug` APK.
