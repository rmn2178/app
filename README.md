# MonoTodo v5 — Smooth Minimalist Monochrome Android To-Do App

Clean black-to-white UI, smooth Release performance without R8 stripping issues, and customizable subtle sounds.

## What Was Fixed in v5
- **Fixed broken 996 KB R8-stripped APK**: Disabled R8 minification/resource shrinking (`isMinifyEnabled = false`, `isShrinkResources = false`) while keeping `isDebuggable = false` so Compose runs at full Release speed without stripping classes or resources.
- **Consistent Signing Keystore**: Bundled `app/monotodo.keystore` so every GitHub Actions build is signed with the same key and installs cleanly over previous builds.
- **Lean Dependencies**: Replaced the 14 MB `material-icons-extended` library with `material-icons-core` for faster builds and a lean, working APK.
- **Updated GitHub Actions**: Migrated to `actions/setup-java@v5`.

## Install Note
If you previously installed an older debug-signed version of **MonoTodo**, uninstall it once first so Android allows installing the newly signed Release APK. Future updates will install directly on top without uninstalling.
