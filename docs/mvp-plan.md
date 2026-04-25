# RoadShake — MVP plán (6 týdnů)

Tento dokument převádí vstupní zadání do realizovatelného technického plánu pro první verzi produktu.

## 1) Cíle MVP (měřitelné)

- Mobilní klient (Android + iOS přes Flutter) sbírá `accelerometer`, `gyroscope`, `GPS` během jízdy.
- Na zařízení počítá 1s agregace (`acc_rms`, `gyro_rms`) a odesílá anonymizované dávky.
- Backend ukládá časoprostorová data, agreguje je do 10m segmentů a denně klasifikuje úseky na `OK` / `ROUGH`.
- Web dashboard zobrazuje heatmapu a umožní export GeoJSON/CSV.
- Pilotní cíl: identifikovat nejhorších 10 % úseků testované sítě.

## 2) Doporučená architektura (MVP varianta)

### Mobil
- **Framework:** Flutter (jedna codebase pro Android/iOS).
- **Senzory:**
  - `accelerometer` ≥ 50 Hz
  - `gyroscope` ≥ 50 Hz
  - `GPS` 1 Hz
- **Pipeline v zařízení:**
  1. synchronizace timestampů,
  2. high-pass + low-pass filtrace (0.5–20 Hz),
  3. odhad gravitační složky,
  4. 1s okna a výpočet RMS,
  5. outlier clipping (např. 99.5 percentil).
- **Battery policy:** při rychlosti `< 10 km/h` snížit sampling (např. 10 Hz) nebo pozastavit.

### Cloud
- **Ingest:** HTTPS endpoint (Cloud Run / Lambda + API Gateway).
- **Storage:** TimescaleDB (PostgreSQL + PostGIS) s hypertable.
- **Batch ETL:** Python job (každých 24h) pro map matching + spatial binning 10 m.
- **Model:** scikit-learn (RandomForest/GradientBoosting), inferenční job 1× denně.

### Dashboard
- **Frontend:** React + Leaflet + heat layer.
- **API:**
  - `GET /segments?bbox=...&from=...&to=...`
  - `GET /exports/geojson?...`
  - `GET /exports/csv?...`

## 3) Datový model

### Mobilní upload payload

```json
{
  "device_id_hash": "sha256:...",
  "records": [
    {
      "timestamp_unix_ms": 1764212345,
      "lat": 50.0873,
      "lon": 14.4200,
      "speed_kmh": 48.2,
      "acc_rms": 0.37,
      "gyro_rms": 0.05,
      "phone_model": "SM-A546B",
      "mount_position": "holder"
    }
  ]
}
```

### SQL schéma (MVP)

- `raw_measurements`
  - `id bigserial pk`
  - `device_id_hash text`
  - `ts timestamptz`
  - `geom geography(point, 4326)`
  - `speed_kmh double precision`
  - `acc_rms double precision`
  - `gyro_rms double precision`
  - `phone_model text`
  - `mount_position text`

- `road_segments_daily`
  - `segment_id text`
  - `day date`
  - `sample_count int`
  - `acc_rms_median double precision`
  - `gyro_rms_median double precision`
  - `rough_score double precision`
  - `quality_label text` (`OK` / `ROUGH`)

## 4) API kontrakt (minimum)

### `POST /v1/telemetry/batch`

- Auth: Firebase anonymous token (Bearer).
- Komprese: `Content-Encoding: gzip`.
- Limity:
  - max payload 1 MB (po kompresi),
  - fallback upload každých 5 minut.
- Odpověď: `{ "accepted": n, "rejected": m }`.

### `GET /v1/map/heat`

- Query: `bbox`, `from`, `to`, `zoom`.
- Return: GeoJSON s atributy `rough_score`, `quality_label`, `sample_count`.

## 5) ML pipeline (MVP pragmaticky)

1. **Feature engineering (segment/day):**
   - median/mean/std `acc_rms`,
   - `p90(acc_rms)`,
   - median `speed_kmh`,
   - počet průjezdů (`sample_count`).
2. **Labeling pro pilot:**
   - počátečně rule-based threshold (např. `acc_rms_median > T`),
   - postupně nahradit supervised modelem z ručně auditovaných úseků.
3. **Model:** `GradientBoostingClassifier`.
4. **Metriky:** precision/recall/F1 pro třídu `ROUGH`, plus top-10% hit-rate.

## 6) Privacy, legal, GDPR

- Pseudonymizace: hash zařízení + rotace salt po období (např. 30 dní).
- Žádné osobní identifikátory, žádný kontakt uživatele v telemetrii.
- In-app consent obrazovka s vysvětlením účelu sběru.
- Kill-switch: uživatel může sběr kdykoli zastavit.
- Retence: raw data max 90 dní, agregace 24 měsíců.

## 7) Dodání po týdnech

- **Týden 1–2:** Flutter sběr senzorů + lokální CSV + debug grafy.
- **Týden 3:** zabezpečený ingest endpoint + gzip batch upload.
- **Týden 4:** ETL a 10m segment agregace + threshold label.
- **Týden 5:** první model + validace na pilotní trase.
- **Týden 6:** dashboard, heatmapa, exporty, demo report.

## 8) Akceptační kritéria MVP

- Aplikace stabilně sbírá data během 30min jízdy bez pádu.
- Upload je spolehlivý i při výpadku sítě (retry/backoff, lokální fronta).
- Dashboard vykreslí segmenty pro zvolené období do 2 s (pilotní dataset).
- Report umí označit nejhorších 10 % úseků + export GeoJSON/CSV.

## 9) Rizika a mitigace

- **Umístění telefonu:** explicitní volba `holder/pocket/other` jako feature.
- **Různé senzory:** normalizace podle modelu + robustní metriky (median/p90).
- **Falešné pozitivy:** vyžadovat více průjezdů a agregovat mediánem.
- **Nízká participace:** jednoduchá gamifikace (body/km), partnerství s městy.


## 10) Referenční trasa pro kalibraci (doplnění pro pilot)

Pro první ladění modelu doporučujeme připravit krátkou referenční trasu a projet ji opakovaně (alespoň 5–10 průjezdů každým směrem):

- **Polní cesta / velmi hrubý povrch** (očekávaná třída `ROUGH`)
- **Rozbitá městská cesta** s výtluky/nerovnostmi (očekávaná třída `ROUGH`)
- **Hladká kvalitní cesta** (nový asfalt, očekávaná třída `OK`)

Doporučený postup:

1. Každý úsek označit v mapě a uložit jako `reference_segment_id`.
2. U každého průjezdu uložit i `mount_position` (držák/kapsa), aby šlo oddělit vliv umístění telefonu.
3. Z referenční trasy vytvořit počáteční validační dataset pro nastavení thresholdu a první verzi modelu.
4. Jako základní kontrolu použít, že model správně odliší hladký úsek od polní cesty s vysokou přesností.


## 11) S čím začít (praktické pořadí)

Doporučený start je **nejdřív jednoduchá Android testovací appka + lokální logování**, a teprve potom backend.

### Proč takto

- Nejdřív ověříš, že senzory na reálných trasách dávají použitelný signál.
- Rychle vyladíš sampling, filtry, mount position a prahy bez cloudové složitosti.
- Získáš referenční dataset, na kterém se pak backend i model ladí mnohem snadněji.

### Fáze 0 (1–2 týdny)

1. Android app (debug build) se sběrem akcelerometru, gyra a GPS.
2. Lokální ukládání po 1s oknech do CSV/JSON.
3. Tlačítka Start/Stop + export souboru.
4. Ověření na referenční trase (hladká/rozbitá/polní).

### Fáze 1 (navázání)

1. Přidej batch uploader (gzip + HTTPS) do jednoduchého ingest endpointu.
2. Ukládej data do jedné `raw_measurements` tabulky.
3. Až pak řeš agregaci, dashboard a model.

Pokud je cílem rychlá validace nápadu, **backend v první iteraci může být minimální** (jen příjem + uložení), zatímco většina práce půjde do kvality sběru v mobilu.


## 12) Hlasové štítky z mobilu (pro budoucí učení)

MVP může ukládat i jednoduché hlasové značky od řidiče:

- kontext trasy: `dálnice`, `běžná silnice`, `rozbitá silnice`, `polní/lesní cesta`, `vlakový přejezd`, `retardér`,
- potvrzení události po přejezdu: `ANO` / `NE`.

Tyto značky lze přidat do trénovacích dat jako weak labels a postupně zlepšovat detekci falešných pozitiv.

