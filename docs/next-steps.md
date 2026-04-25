# RoadShake — Co dál (praktický plán na další dny)

Tento plán navazuje na aktuální scaffold Android aplikace.

## Cíl příští iterace (Sprint 1)

Přepnout aplikaci ze syntetických dat na **reálný sběr** z telefonu a mít první použitelný export z 30min trasy.

## Priorita P0 (udělat hned)

1. **Napojit reálné senzory (ACC + GYRO) přes `SensorManager`**
   - frekvence min. 50 Hz,
   - synchronizace timestampů,
   - buffer do 1s oken.
2. **Napojit GPS (1 Hz)**
   - Fused Location Provider,
   - ukládat `lat`, `lon`, `speed_kmh`.
3. **Runtime permissions flow**
   - `ACCESS_FINE_LOCATION`, `RECORD_AUDIO`.
4. **Nahradit fake data v `CollectorEngine` reálnou agregací**
   - RMS za 1s,
   - speed gating pod 10 km/h.

## Priorita P1 (hned po P0)

1. **Session metadata**
   - `driver_note`, `route_id`, `mount_position`.
2. **Voice UX zjednodušení**
   - auto-prompt po detekci anomálie,
   - timeout a fallback při nerozpoznání.
3. **Export UX**
   - tlačítko „Sdílet CSV“ (Android share sheet).

## Priorita P2 (po pilotu)

1. Minimal ingest endpoint (HTTPS + gzip).
2. Batch upload po 5 minutách nebo 1 MB.
3. První dashboard s mapou a barevným `rough_score`.

## Konkrétní Definition of Done pro Sprint 1

- 30min jízda bez pádu appky.
- CSV obsahuje reálná data (ne fake) po 1s intervalech.
- Rozdíl hladká vs. rozbitá trasa je v `acc_rms` viditelný.
- Hlasové označení typu cesty se ukládá do `road_context`.

## Návrh pořadí implementace (2–3 dny)

### Den 1
- SensorManager + buffering + RMS.

### Den 2
- GPS + permission flow + speed gating.

### Den 3
- Ověření na trase + ladění filtrů + export a kontrola dat.
