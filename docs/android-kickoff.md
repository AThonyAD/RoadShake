# Android kickoff pro RoadShake (MVP sběr dat)

Ano — můžeme začít hned. Níže je minimum, které potřebuji, abych připravil první funkční prototyp sběru dat v Android aplikaci.

## 1) Co potřebuji od tebe

### Rozhodnutí (produkt)
- **Cílový Android min SDK:** doporučení `26+`.
- **Umístění telefonu pro pilot:** držák na skle / palubce / kapsa (vyber primární variantu).
- **Pilotní trasa:** 1 hladký, 1 rozbitý, 1 hrubý úsek (ideálně každý 1–3 km).
- **Formát výstupu v první iteraci:** CSV nebo JSON (doporučuji CSV + JSON metadata).

### Technické vstupy
- Potvrzení, že začínáme **offline-first** (bez backendu v 1. kroku).
- Jestli chceš nativní Android (Kotlin) nebo Flutter. Pro rychlý Android pilot doporučuji **Kotlin**.
- Název balíčku (např. `cz.roadshake.collector`) a interní název appky.

## 2) Co dodám v první iteraci (1–2 týdny)

- Android app s tlačítky `Start` / `Stop`.
- Sběr dat:
  - accelerometer (`>=50 Hz`),
  - gyroscope (`>=50 Hz`),
  - GPS (`1 Hz`).
- Lokální výpočet 1s oken: `acc_rms`, `gyro_rms`, `speed_kmh`.
- Battery policy: při rychlosti `<10 km/h` snížení samplingu.
- Export logu do souboru (CSV) + metadata (JSON).

## 3) Definice MVP záznamu (1 řádek CSV)

```text
ts_unix_ms,lat,lon,speed_kmh,acc_rms,gyro_rms,phone_model,mount_position,road_context
```

## 4) Akceptační kritéria pro první build

- 30 min jízda bez pádu aplikace.
- Vznikne exportovatelný soubor se záznamy po 1 sekundě.
- V logu je jasně vidět rozdíl mezi hladkou a rozbitou trasou.

## 5) Doporučený další krok po schválení

Jakmile potvrdíš body z části **1)**, začnu scaffoldem projektu a připravím:

1. architekturu appky,
2. službu pro sběr senzorů,
3. jednoduchou obrazovku Start/Stop,
4. ukládání CSV/JSON,
5. stručný návod na pilotní sběr dat.


## 6) Potvrzená rozhodnutí (2026-04-25)

- Platforma: **Kotlin (nativní Android)**.
- Umístění telefonu: **držák u středového tunelu / držák na nápoje** s gumovou podložkou.
- Pilotní trasa: **schváleno** (hladká / rozbitá / hrubá).
- Výstup: **CSV ano** (JSON metadata doplňkově).

### Poznámka k umístění v držáku na nápoje

Ano, je to použitelné pro pilot. Jen doporučuji držet se jedné konzistentní konfigurace během sběru:

1. stejný držák,
2. stejná gumová podložka,
3. stejná orientace telefonu,
4. stejná poloha zařízení v držáku.

Tím se sníží variabilita a bude snazší porovnávat průjezdy mezi sebou.



## 7) Hlasové ovládání (hands-free)

Pro bezpečnější používání při řízení je součástí MVP hlasové ovládání:

- hlasové nastavení kontextu úseku: `dálnice`, `běžná silnice`, `rozbitá silnice`, `polní/lesní cesta`, `vlakový přejezd`, `retardér`,
- hlasový dotaz po detekci anomálie: „Byla to překážka? ANO/NE“,
- výsledek potvrzení ukládat jako label pro budoucí trénink modelu.
