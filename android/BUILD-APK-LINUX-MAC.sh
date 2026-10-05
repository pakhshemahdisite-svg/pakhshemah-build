#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"
: "${ANDROID_HOME:=${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}}"
if [[ ! -f "$ANDROID_HOME/platforms/android-35/android.jar" ]]; then
  echo "Android SDK Platform 35 is required. Install it from Android Studio SDK Manager."
  exit 1
fi
if command -v gradle >/dev/null 2>&1; then
  gradle --no-daemon clean assembleDebug
else
  echo "Gradle 8.9 is required. On Windows use BUILD-APK-WINDOWS.bat, which downloads it automatically."
  exit 1
fi
cp app/build/outputs/apk/debug/app-debug.apk PakhshMahdi-v0.1.1-debug.apk
echo "APK created: $ROOT/PakhshMahdi-v0.1.1-debug.apk"
