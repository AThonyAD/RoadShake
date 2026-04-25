# RoadShake Android app (MVP scaffold)

První implementační scaffold pro Android (Kotlin).

## Co je hotové

- Jednoduchá Compose obrazovka se stavem sběru (`Start`/`Stop`).
- `CollectorEngine` s 1s tickem a zápisem do CSV.
- Datový model `TelemetryRecord` odpovídající MVP schématu.
- Lokální soubor `roadshake_session.csv` v `filesDir`.
- Hlasové ovládání (speech-to-text) pro nastavení kontextu:
  - „dálnice“
  - „běžná silnice“
  - „rozbitá silnice“
  - „polní/lesní cesta“
  - „vlakový přejezd“
  - „retardér"
- Hlasové potvrzení překážky `ANO/NE` (text-to-speech prompt + speech input).

## Co je zatím placeholder

- Reálný sběr z akcelerometru, gyroskopu a GPS ještě není napojen.
- `CollectorEngine` aktuálně generuje syntetické hodnoty pro rychlý end-to-end test pipeline (UI -> CSV).

## Další krok

- Napojit Android `SensorManager` (ACC/GYRO) a Fused Location Provider (GPS),
- spočítat 1s RMS z reálných vzorků,
- implementovat speed gating pod 10 km/h,
- přidat runtime permission flow (location + microphone).


## Projektový postup

- Praktický plán dalších kroků je v `docs/next-steps.md`.


## Instalace do telefonu

- Postup je popsaný v `docs/android-phone-install.md`.
- Rychlá instalace: `bash android-app/scripts/install_debug.sh`.
