# Jak dostat RoadShake appku do Android telefonu

Níže je nejrychlejší postup pro debug build (USB kabel + ADB).

## 1) Co je potřeba

- Android telefon
- USB kabel
- Zapnuté **Developer options** a **USB debugging**
- `adb` (Android platform-tools)
- Android build toolchain (Android Studio nebo `gradle` + Android SDK)

## 2) Zapnutí USB debug režimu (telefon)

1. Nastavení → O telefonu → 7× klepnout na „Číslo sestavení“.
2. Otevřít „Možnosti pro vývojáře“.
3. Zapnout „Ladění USB“.
4. Po připojení kabelem potvrdit otisk počítače („Always allow“).

## 3) Rychlá instalace skriptem (doporučeno)

V rootu repozitáře spusť:

```bash
bash android-app/scripts/install_debug.sh
```

Skript:
- sestaví debug APK,
- počká na zařízení přes `adb`,
- nainstaluje aplikaci (`adb install -r ...`).

## 4) Ruční cesta (když chceš kroky zvlášť)

### 4.1 Build APK

V `android-app/`:

```bash
./gradlew assembleDebug
```

Pokud nemáš `gradlew`, můžeš použít:

```bash
gradle assembleDebug
```

### 4.2 Ověření telefonu přes ADB

```bash
adb devices
```

Měl bys vidět zařízení ve stavu `device`.

### 4.3 Instalace APK

```bash
adb install -r android-app/app/build/outputs/apk/debug/app-debug.apk
```

## 5) První spuštění v telefonu

1. Otevři appku **RoadShake Collector**.
2. Povol oprávnění (poloha + mikrofon).
3. Klikni `Start` a zkus hlasové ovládání.

## 6) Kde najít CSV log

Pro debug můžeš vytáhnout soubor přes `adb`:

```bash
adb shell run-as cz.roadshake.collector cat files/roadshake_session.csv > roadshake_session.csv
```

## 7) Nejčastější problémy

- `device unauthorized` → potvrdit RSA dialog v telefonu.
- `adb: command not found` → doinstalovat platform-tools a přidat do PATH.
- `INSTALL_FAILED_VERSION_DOWNGRADE` → odinstalovat appku nebo použít vyšší `versionCode`.
