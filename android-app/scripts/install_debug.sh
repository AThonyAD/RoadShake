#!/usr/bin/env bash
set -euo pipefail

APP_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APK_PATH="$APP_DIR/app/build/outputs/apk/debug/app-debug.apk"

if ! command -v adb >/dev/null 2>&1; then
  echo "[ERROR] adb není dostupné. Nainstaluj Android platform-tools." >&2
  exit 1
fi

cd "$APP_DIR"

if [ -x "./gradlew" ]; then
  ./gradlew assembleDebug
elif command -v gradle >/dev/null 2>&1; then
  gradle assembleDebug
else
  echo "[ERROR] Není k dispozici gradlew ani gradle." >&2
  exit 1
fi

if [ ! -f "$APK_PATH" ]; then
  echo "[ERROR] APK nenalezeno: $APK_PATH" >&2
  exit 1
fi

echo "[INFO] Čekám na zařízení..."
adb wait-for-device

echo "[INFO] Instaluji APK..."
adb install -r "$APK_PATH"

echo "[OK] Hotovo. Spuštění appky zkus přes launcher: RoadShake Collector"
