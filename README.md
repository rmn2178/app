# MonoTodo v2 — Ultra-Smooth Animated Monochrome Android To-Do App

An offline-first Android To-Do app crafted with a **black-to-white vertical gradient theme**, **GPU-accelerated Jetpack Compose animations**, **haptic feedback**, and **custom low-latency SoundPool sound effects**.

## What's Inside
- **Living Black-to-White Gradient**: Drifting obsidian-to-pure-white backdrop with subtle ambient monochrome light orbs.
- **Smooth Animations**:
  - Staggered spring card entrances (`graphicsLayer`) and smooth `animateItemPlacement` reordering
  - Animated circular progress ring (`Canvas` sweep gradient), glossy shimmering progress bar, and vertical-sliding counters
  - Bouncy tactile press feedback (`bounceClick`), rotating `+` FAB, and radial sparkle burst when completing tasks
  - Full-screen monochrome particle burst when hitting 100% completion
- **Custom Sound Effects (`SoundPool`)**: 6 synthesized `.wav` effects (`sfx_complete`, `sfx_uncheck`, `sfx_add`, `sfx_delete`, `sfx_tap`, `sfx_all_done`) with a persistent mute/unmute toggle in the header.
- **Performance**: `@Immutable` models, `derivedStateOf` filtering, `contentType` + stable keys in `LazyColumn`, and `graphicsLayer` transforms to keep animations silky smooth.

## Build the `.apk`
- **GitHub Actions**: Push the unzipped folder to a GitHub repo (`main` branch), open the **Actions** tab, and download the **`monotodo-debug-apk`** artifact (`app-debug.apk`).
- **Android Studio**: Open the unzipped `android-todo-monochrome` folder and run **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
