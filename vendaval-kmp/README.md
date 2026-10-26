# Vendaval KMP

Kotlin Multiplatform port of the Vendaval cultural-spaces directory for the state of
Aguascalientes, targeting **Android, iOS and desktop** from one Compose Multiplatform
codebase.

The original is a three-file static site (`index.html`, `app.js`, `styles.css`) built by
Colectivo Vendaval with the IAJU. This is not a wrapper around it: the dataset, the
taxonomy, the visual language and the Supabase schema were carried over, and everything
else — persistence, search, mapping, submission flow — was rebuilt natively.

---

## Layout

```
vendaval-kmp/
├── shared/                     Domain + data + DI. No UI, no Compose.
│   └── src/
│       ├── commonMain/         Models, use cases, repositories, Ktor, okio
│       ├── androidMain/        LocationManager, Intents, Context-backed storage
│       ├── jvmMain/            AWT Desktop, XDG/Library paths
│       ├── iosMain/            Core Location, UIKit hand-off, sandbox paths
│       └── commonTest/         69 tests
└── composeApp/                 Compose Multiplatform UI + platform entry points
    └── src/
        ├── commonMain/         Theme, i18n, navigation, map, 6 screens
        ├── androidMain/        Activity, Application, manifest, resources
        ├── jvmMain/            Window entry point
        ├── iosMain/            UIViewController bridge
        ├── commonTest/         Projection tests
        └── jvmTest/            Tile pipeline integration test
```

### Dependency direction

```
composeApp  ──▶  shared/domain  ◀──  shared/data
                     ▲
                 (interfaces only)
```

Screens depend on domain types and repository *interfaces*. Platform capabilities
(location, launching, storage) are domain interfaces implemented per target and supplied
by Koin, so no UI code and no domain code contains a platform import.

---

## Feature set

| Area | What it does |
|---|---|
| Directory | Accent-insensitive full-text search over name, address, type and municipality; multi-select filters by venue type and by municipality; four sort modes; live result count |
| Map | Custom OpenStreetMap raster renderer on a Compose `Canvas` — pan, pinch and scroll-wheel zoom, colour-coded markers, tap-to-select with nearest-candidate hit testing, viewport culling, legend, attribution |
| Detail | Full record, call, open in the platform map app, share, show on map, favourite, delete for local records |
| Near me | Haversine distance ordering from a device fix, with a municipality centre as an explicit fallback where there is no GPS |
| Favourites | Device-local, offline, independent of the active search |
| Submission | Validated form with field-level errors and warnings, two coordinate shortcuts, offline-durable write with a retry outbox |
| Coverage | Counts by type and by municipality derived from live data, including the four municipalities with zero venues |
| Sync | Supabase PostgREST mirror plus a realtime `INSERT` subscription over the Phoenix WebSocket protocol, with typed failure states |
| Settings | Theme, language (es/en), manual refresh, platform diagnostics |
| Adaptive layout | Bottom bar under 600 dp, navigation rail from 600 dp, rail plus persistent detail pane from 840 dp |

---

## Decisions worth defending

### A hand-written map instead of a map SDK

No map SDK covers all three targets: Google Maps is Android-only, MapKit is Apple-only,
desktop has neither. The alternatives were three `expect`/`actual` map implementations
(three APIs to learn, three sets of bugs, desktop left out) or one shared renderer.

`TileMath` + `TileStore` + `MapCameraState` + `OsmMap` is roughly 600 lines and gives
identical behaviour everywhere. Cost: no vector tiles, no turn-by-turn, no 3D. For a
directory that needs to show ~30 pins over a city, none of that was needed.

**Tile policy caveat:** the OSM public tile servers are used with an identifying
`User-Agent` and an in-memory cache, which their usage policy permits for low-volume
clients. A real deployment should point `TileSource.urlTemplate` at its own tile server.

### A JSON file cache instead of SQLDelight

The catalogue is a bounded set of a few hundred records that the app always loads in full
to filter and to plot. A query engine buys nothing there, while SQLDelight adds a codegen
step and three platform-specific native drivers.

`JsonFileStore` writes through a temp file and an atomic move, so a crash mid-write cannot
corrupt the cache. Everything sits behind `CatalogueCache` / `FavouritesCache` /
`PreferencesCache`, so swapping in SQLDelight later touches no caller. If the dataset ever
reaches the tens of thousands, that swap is the intended path.

### Typed cache wrappers instead of DI qualifiers

Registering three `JsonFileStore<T>` singletons **does not work**: generics are erased, so
the container treats them as one key and hands back the wrong one. This was found by
running the app — it surfaced as a `ClassCastException` on first read, far from the wiring
that caused it. Qualifiers would have hidden it behind runtime strings; distinct types make
it a compile error instead.

### A hand-rolled navigator and state holder

`navigation-compose` and `lifecycle-viewmodel` were both still pre-release for
multiplatform when this was written. The app needs a tab selection plus one overlay, and a
coroutine scope that dies with its composition. That is ~150 lines, so it is owned outright
rather than taken as an alpha dependency on the critical path.

### Offline-first as a hard contract

`SpaceRepository.spaces` always emits usable data and never waits on the network. Three
sources merge in increasing order of authority: the compiled-in seed, the persisted remote
mirror, then unpublished local records in a negative id space. `refresh()` never throws and
never empties the list; failure downgrades a badge.

This is load-bearing, not theoretical. **The Supabase project the original site points at
no longer resolves** (`ebjejctuuhvmjmevabjh.supabase.co` → `NXDOMAIN`), so the app runs
permanently in its fallback path today and is fully functional there.

---

## Security notes

- **The anon key is public by design.** It is Supabase's publishable key and the web portal
  already serves it in plain JavaScript. The real control is row-level security. **Write
  access to `espacios_culturales` must be constrained by an RLS policy** — without one,
  anybody holding this key, which is everybody, can insert arbitrary rows. The key lives in
  one reviewable place (`SupabaseConfig.PRODUCTION`) and is injected, never global.
- Cleartext traffic is denied on Android via `network_security_config.xml`.
- Submitted text is length-capped and rejected if it contains markup.
- The tile client deliberately does not carry the Supabase auth headers to a third-party CDN.
- Location permission is optional everywhere; every screen degrades cleanly without it.

---

## Building

Requires JDK 17 or 21 and the Android SDK. On the machine this was developed on:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME="$HOME/Library/Android/sdk"
```

```bash
./gradlew :composeApp:assembleDebug     # Android APK
./gradlew :composeApp:run               # Desktop
./gradlew :shared:jvmTest :composeApp:jvmTest
```

iOS is off by default because it needs a full Xcode installation. See
[`iosApp/README.md`](iosApp/README.md).

### Version matrix

Kotlin 2.0.21 · Compose Multiplatform 1.7.3 · AGP 8.7.3 · Gradle 8.12 · JDK 21 host,
JVM 17 bytecode · compileSdk 35, minSdk 26 · Ktor 3.0.3 · Koin 4.0.0 · okio 3.9.1

Every artifact is published against Kotlin 2.0.21, which keeps metadata resolution stable
across the three targets.

The build emits one warning: AGP 8.7.3 is above the highest version the Kotlin 2.0.21
Gradle plugin was tested against (8.5). It is benign here — Android compilation, resource
merging and APK packaging all succeed — and is documented rather than silenced so it stays
visible when the toolchain is next upgraded.

---

## Verified status

| Check | Result |
|---|---|
| `:composeApp:assembleDebug` | Passing — 18 MB debug APK |
| `:composeApp:compileKotlinJvm` | Passing |
| `:shared:jvmTest` | 69 tests, 0 failures |
| `:composeApp:jvmTest` | 11 tests, 0 failures |
| Desktop app launch | Runs with 0 exceptions; directory screen verified visually |
| Tile pipeline | Real OSM tile fetched and decoded to a 256×256 bitmap |
| iOS targets | **Not compiled** — needs Xcode, unavailable on the build machine |

Two runtime bugs were found by actually running the app rather than by reading it: the
`Clock` binding registered under its concrete type, and the erased-generic cache collision
described above. Both are fixed.
