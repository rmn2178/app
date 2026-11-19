# MonoTodo — Animated Monochrome Android To-Do App

An animated, offline-first Android To-Do app crafted with a continuous **black-to-white vertical gradient theme** in **Kotlin + Jetpack Compose (Material 3)**.

## Highlights
- **Black-to-White Gradient Theme**: Smooth animated obsidian-to-pure-white gradient backdrop with high-contrast monochrome cards, frosted progress header, and inverted black/white priority pills.
- **Fluid Animations**:
  - Spring-based list reordering (`animateItemPlacement`) when completing or prioritizing tasks
  - Bouncy animated checkbox fill and strikethrough state transitions
  - Animated progress bar and vertical-sliding task counter
  - Smooth `ModalBottomSheet` task editor with animated pill selectors
  - Gentle breathing gradient and pulsing empty-state emblem
- **Offline Storage**: Persists all tasks automatically using Android `SharedPreferences`.

## Build the `.apk`
- **Android Studio**: Unzip `android-todo-monochrome.zip`, open the `android-todo-monochrome` folder in Android Studio, and click **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
- **GitHub Actions (Cloud Build)**: Push the unzipped folder to a GitHub repository (`main` branch), open the **Actions** tab, and download the **`monotodo-debug-apk`** artifact (`app-debug.apk`).
