# AGENTS.md

Guidance for AI coding agents working on this repository.

## Project overview

**DatMusic** — a third-party music search/streaming client for the DatMusic service (datmusic.xyz).
Android app, Jetpack Compose UI, published on Google Play as `tm.alashow.datmusic`.
Closed-source, single-developer project (Alashov Berkeli, `tm.alashow`).

- API base: `https://api-demo.datmusic.xyz/` (defined in `modules/core-domain/src/main/java/tm/alashow/Config.kt`)
- Current version: see `buildSrc/src/main/java/tm/alashow/buildSrc/App.kt` (versionCode/versionName live there)
- Play listing data: `app/src/main/play/` (title, description, screenshots, subscription products)
- App store art assets: `art/` (screenshots source/framed, feature graphic psd, dependency graph)

## Tech stack

- Kotlin (currently a beta version — check `gradle/libs.versions.toml`), Gradle 9.x, AGP 9.x
- Java 11 source/target compatibility; CI builds with JDK 21
- Jetpack Compose (dual BOM: stable + alpha in `libs.versions.toml`), Material3
- Hilt (Dagger 2) for DI; KSP for Hilt + lifecycle codegen
- Navigation Compose + Accompanist modal bottom sheets
- Room 2.8 (single `AppDatabase` in `:modules:core-data`), incl. FTS table for audio
- Retrofit + OkHttp + kotlinx-serialization (API in `DatmusicEndpoints`)
- Media3 ExoPlayer (playback), Fetch (downloads), Store5 (caching/preferences)
- Firebase: Crashlytics, FCM, Remote Config, Analytics; billing via Qonversion
- Coil (images), Timber (logging), threeTenAbp (dates)
- Testing: JUnit4, Truth, Robolectric, MockK, Turbine
- Code style: Spotless + ktlint 0.41.0 (android mode, max line length 200, wildcard imports allowed), license header required on all `.kt` files

## Repository layout

Multi-module Gradle project. `settings.gradle` is the source of truth for the module list.
Dependency direction (top depends on bottom):

```
app  →  ui-* (feature screens), core-ui* (shared UI), navigation, core-playback
core-ui-*  →  core-ui, common-*, navigation
core-playback / core-library / core-downloader / core-media  →  core-data → core-domain
core-data  →  common-data → common-domain → base
base-android  →  base  →  i18n
```

### Modules (all under `modules/`)

**Foundation**
- `base` — platform-agnostic utils: `Analytics`, date utils, extensions, `SnackbarManager`, `CoroutineDispatchers`, serializers. Re-exports `i18n` resources.
- `base-android` — Android-specific: Qonversion billing (`billing/`), Coil image loading (`imageloading/`), app initializers (Timber, threeTenAbp), `ViewState` base UI.
- `i18n` — **all UI strings live here** (`src/main/res/values*/app_strings.xml`, `base.xml`). Locales: en (default), ru, tk, tr, fr, es, de. Also `UiMessage`, `Validation`, `TextCreator`.
- `common-domain` — shared domain models: `Async`, `InvokeStatus`, `Optional`, `Resource`, `BaseEntity`, `SortOption`, `Params`.
- `common-data` — shared data infrastructure: `Interactor` base class (see below), `PreferencesStore`, `RemoteConfig`, `RoomRepo`, `DatabaseTxRunner`, `LocalFilesRepo`, `LastRequests`.
- `common-testing` / `common-testing-instrumented` — `BaseTest`, `TestImageModule`, Turbine extensions.

**Core (business logic, no UI)**
- `core-domain` — DatMusic entities: `Audio`, `Artist`, `Album`, `Playlist`, `DownloadRequest`, `Genre`, `LibraryItem`; `ApiResponse`, API errors; **`tm.alashow.Config`** (BASE_URL, API_BASE_URL, timeouts, Play Store id).
- `core-data` — Room `AppDatabase` + DAOs (`daos/`: Audios, AudiosFts, Artists, Albums, Playlists, PlaylistsWithAudios, DownloadRequests), migrations (`db/Migrations.kt`), Retrofit `DatmusicEndpoints` + `ApiModule`, FCM registration interactor.
- `core-media` — artist/album/audio/search data: repos, data sources, Store-based caching, paging sources, interactors (`interactors/`), observers (`observers/`).
- `core-library` — playlists: `PlaylistsRepo`, playlist interactors (create/update/delete/download/addTo), backup & restore (`interactors/backup/`), FTS migration.
- `core-downloader` — Fetch-based `DownloaderImpl`, `DownloadManager`, `DownloadRequestsRepo`, download observers/messages/notifications.
- `core-playback` — Media3: `PlayerService` (media session), `DatmusicPlayer`/`AudioPlayer`, `MediaQueueBuilder`, audio focus, media button & becoming-noisy receivers, `PlaybackConnection`.

**UI**
- `common-compose` — Compose utilities: `rememberFlowWithLifecycle`, composition locals (`LocalAnalytics`, `LocalSnackbarHostState`, `LocalIsPreviewMode`), preview wrappers (`CombinedPreview`, `LocalePreview`, `FontScalePreview`, `DevicePreview`), Recompose Highlighter.
- `common-ui-theme` — `AppTheme`, `ThemeViewModel`, color/typography/shape specs, Material bridges.
- `common-ui-components` — generic composables: swipes, material wrappers, list items.
- `core-ui` — cross-feature UI glue: `AudioActionHandler`, `AudioDownloadItemActionHandler`, snackbar host, **preview fakes** (`ui/previews/Preview*.kt`) used by previews in every other module.
- `core-ui-media` — shared artist/album/audio list UI (items used in search results etc.).
- `core-ui-playback` — mini player controls, playback sheets UI.
- `core-ui-downloader` — download progress UI components.
- `core-ui-library` — playlist UI (add-to-playlist dialog etc.).
- `ui-search` — search screen (root tab).
- `ui-artist` — artist details screen.
- `ui-album` — album details screen.
- `ui-library` — library root tab + playlist create/edit/detail screens.
- `ui-downloads` — downloads root tab + per-audio download UI.
- `ui-settings` — settings root tab: theme, backup/restore, premium (Qonversion).
- `navigation` — `Navigator`/`NavigatorViewModel`, bottom-sheet navigator, **`screens/Screens.kt`** (all root + leaf routes, args, deep links).

**App shell** (`app/`)
- `ui/DatmusicApp.kt` — composition root (theme → navigator → downloader host → playback host → action handlers → content).
- `ui/home/Home.kt` — bottom nav / wide-layout nav rail + mini player + scaffold.
- `ui/MainActivity.kt`, `ui/Intents.kt` (deep links), `di/AppModule.kt`, `di/NetworkModule.kt`.
- `fcm/`, `notifications/`, `util/` (Remote Config init, OkHttp interceptors).
- Launcher icons in `src/main/res-drawable/` (extra res dir, see `sourceSets` in `app/build.gradle`).

## Architecture & patterns

- **Layers**: Compose screen → Hilt ViewModel (exposes `Flow` of state) → Interactor/Repo → DataSource (API via Retrofit / Room DAO / Store cache) → domain entities in `core-domain`.
- **`Interactor`** (`common-data/.../Interactor.kt`) is the base for one-shot use cases: `invoke(params): Flow<InvokeStatus>` with timeout + `InvokeStarted/Success/Error` emissions; `execute(params)` for fire-and-forget.
- **Observers** are long-lived `Flow` sources (e.g. `ObservePlaylists`), **Repos** own entity + DAO + cache.
- **Cross-cutting UI state via Composition Locals**, provided in `DatmusicApp.kt`: `LocalPlaybackConnection`, `LocalDownloader`, `LocalAudioActionHandler`, `LocalAudioDownloadItemActionHandler`, `LocalSnackbarHostState`, `LocalAnalytics`. Inject with `LocalX.current` in composables.
- **Navigation**: 4 root tabs (Search, Downloads, Library, Settings) each hosting a nav graph of leaf screens; bottom sheets registered via Accompanist navigator; deep links patterned as `https://datmusic.xyz/...` (artist/album/playlist/search). Add new screens in `navigation/screens/Screens.kt` and wire in `AppNavigation` (`app/.../ui/AppNavigation.kt`).
- **Previews**: every screen composable has a `@CombinedPreview`-annotated preview backed by fakes in `core-ui/ui/previews/` (e.g. `PreviewDatmusicCore`, `PreviewPlaybackConnection`). Keep fakes consistent when adding previews.
- **Theming**: `ThemeViewModel` state → `AppTheme(themeState)`; theme specs (padding, typography) in `common-ui-theme`.

## Build, test, CI

```bash
./gradlew spotlessCheck          # style gate (CI runs this)
./gradlew spotlessApply          # fix style
./gradlew testDebug              # unit tests (CI runs this)
./gradlew test                  # all unit tests
./gradlew assembleDebug          # debug APK
./gradlew assembleRelease        # release APK (R8 + resource shrinking)
./gradlew publishRelease --track=production   # upload to Play (alpha|beta|production)
./gradlew :modules:core-data:test --tests "*AudiosDaoTest*"   # single test
```

- CI (`.github/workflows/android.yml`, `android-pr.yml`): `spotlessCheck` + `testDebug` on push/PR, JDK 21, uploads JUnit reports.
- Release builds also run on Bitrise (`bitrise.yml`); signing material in `signing/` (release keystore may be encrypted as `.aes`, see `signing/decrypt.sh`).
- Play publish uses `com.github.triplet.play` with `signing/play-account.json` (env `PUBLISH_TRACK` overrides track, default `alpha`).
- Release signing env vars: `DATMUSIC_RELEASE_KEYSTORE_PWD`, `DATMUSIC_RELEASE_KEY_PWD`, `DATMUSIC_RELEASE_KEY_ALIAS`.
- Version bumps: edit `buildSrc/src/main/java/tm/alashow/buildSrc/App.kt` (versionCode + versionName). Debug builds get `-DEBUG` suffix and app id `.debug`.

## Conventions & gotchas

- **License header** required on every Kotlin file (template: `spotless/copyright.kt`) — Spotless enforces it.
- ktlint: android style, **200 char max line**, 4-space indent; run `./gradlew spotlessApply` before committing.
- **Strings**: add new user-facing strings to `modules/i18n/src/main/res/values/app_strings.xml` and translate in the other locale folders. Don't hardcode strings in composables.
- **Versions**: everything is in `gradle/libs.versions.toml` (version catalog `libs`); reference as `libs.*`. Don't pin versions in module `build.gradle` files.
- Repos are centralized in `settings.gradle` (`RepositoriesMode.FAIL_ON_PROJECT_REPOS`); note the androidx.dev + sonatype snapshot repos are there intentionally.
- All `subprojects` exclude `appcompat` and Material (old) — Compose only; don't re-add view-based libs.
- `app/build.gradle` uses `prop(...)` to read config from project properties **or env vars** (e.g. `PUBLISH_TRACK`).
- Room migrations must be added explicitly in `AppDatabase` (`AutoMigration` specs live in `core-data/.../db/Migrations.kt`); DAO tests exist under `core-data/src/test/.../daos/` as reference.
- The project tracks cutting-edge dependencies (Kotlin beta, Compose alpha, media3-ext snapshot). Bumps can break the build — prefer minimal version changes and verify with `spotlessCheck testDebug assembleDebug`.
- `.idea/`, `.gradle/`, `modules/build/` are local/IDE noise; ignore unless asked.
- Deep link / share URLs use `Config.BASE_URL` (https://datmusic.xyz) — keep them in sync with `Screens.kt` deep link patterns.
- Firebase config: `app/google-services.json`; FCM token registration interactor is in `core-data` (`interactors/RegisterFcmToken.kt`).
- Backup/restore (library JSON export) is a real feature: `core-library/.../interactors/backup/`, UI in `ui-settings/.../backup/`.

## Quick orientation for common tasks

| Task | Where to look |
|---|---|
| Add a screen | `navigation/screens/Screens.kt` → new `ui-*` module (or extend existing) → `app/.../ui/AppNavigation.kt` |
| Add an API endpoint | `core-data/.../api/DatmusicEndpoints.kt` + models in `core-domain` |
| Change DB schema | `core-data/.../db/AppDatabase.kt` + `Migrations.kt` + DAO tests |
| Playback behavior | `core-playback` (player/service) + `core-ui-playback` (UI) |
| Downloads | `core-downloader` + `ui-downloads` + `core-ui-downloader` |
| Playlists | `core-library` + `ui-library` + `core-ui-library` |
| Theme/colors/typography | `common-ui-theme` |
| New user-facing text | `modules/i18n/src/main/res/values*/` |
| Billing/premium | `base-android/.../billing/` + `ui-settings/.../premium/` + `app/src/main/play/subscriptions/` |
| Release/version | `buildSrc/.../App.kt`, `app/build.gradle` play plugin, `signing/` |
