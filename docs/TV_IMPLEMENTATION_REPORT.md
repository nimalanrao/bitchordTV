# BitChord TV Implementation Report

## 1. Executive Summary

BitChord has been transformed into a universal Android application supporting both handheld devices (smartphones, tablets) and Android TV / Google TV / Fire OS devices from a single unified codebase and application package.

The mobile experience (`MainActivity`, mobile Material 3, touch navigation, widgets, mini-player bottom sheets) remains 100% intact and untouched. The Android TV experience (`TvActivity`, TV Material 3, 10-foot remote-first D-pad navigation, 16:9 two-pane layouts) provides a first-class television listening environment powered by the shared Media3 playback service and audio processing pipelines.

---

## 2. Architecture & Design Decisions

### 2.1 Single-Package Universal Architecture
- **Single Package (`com.music.bitchord`)**: Avoids duplicate codebase maintenance, enables smooth shared storage/downloads, and maintains unified update channels.
- **Dual Launcher Entries**:
  - `MainActivity`: Registered with `android.intent.action.MAIN` + `android.intent.category.LAUNCHER` (portrait, mobile theme).
  - `TvActivity`: Registered with `android.intent.action.MAIN` + `android.intent.category.LEANBACK_LAUNCHER` (landscape, TV theme, singleTask).
- **Dynamic MediaSession PendingIntent**:
  - `PlaybackService.sessionActivity()` inspects `DeviceType.isTv(context)` at runtime. On television devices, notification/Assistant/system Now Playing controls resume `TvActivity`. On mobile devices, they resume `MainActivity`.

### 2.2 Shared Domain & Playback Infrastructure
- **Shared View Model (`MainViewModel`)**: Handles YouTube Music API communication, search queries, filter state, library synchronization, lyrics loading, and account session tokens.
- **Shared Playback Engine (`PlaybackService` & `ExoPlayer`)**:
  - Shared MediaSession, AudioCache, and CrossfadeController (0-12s adjustable gapless crossfade).
  - Native Automix DSP analyzer (Beat This! ONNX runtime model) for beat matching and transitions.
  - QuickJS JavaScript runtime for pluggable Hi-Res stream module resolution (FLAC/ALAC).
  - Discord Rich Presence WebSocket gateway and Last.fm / ListenBrainz scrobblers.

### 2.3 TV Presentation Layer & Design System
- **Pure TV Material 3 (`androidx.tv.material3.MaterialTheme`)**: Avoids Material split-brain by isolating TV UI components under `com.music.bitchord.ui.tv.*`.
- **Remote-First Focus Engine**:
  - 5-Way D-pad focus state machine with 1.05x smooth scale transitions, vibrant `#FA2D48` border halos, and elevation.
  - Spatial navigation trapping on dialogs and overlays with deterministic Back-key dismissal hierarchy.
  - Compose Foundation `LazyRow` and `LazyColumn` for horizontal shelves and vertical catalogs, avoiding deprecated Leanback/TvLazy widgets.
- **10-Foot UI Typography & Layout**:
  - Base design targeting 960 × 540 dp with responsive scaling across 720p, 1080p, and 4K outputs.
  - Safe margins (48 dp horizontal, 27 dp vertical) preventing display overscan clipping.

---

## 3. Component & File Inventory

| Component | File Path | Description |
|---|---|---|
| **TV Activity Entry Point** | `app/src/main/java/com/music/bitchord/TvActivity.kt` | Dedicated Leanback launcher activity, edge-to-edge window configuration. |
| **TV App Shell** | `app/src/main/java/com/music/bitchord/ui/tv/TvApp.kt` | Collapsible TV navigation rail, global mini-player, destination state machine. |
| **Device Classifier** | `app/src/main/java/com/music/bitchord/ui/tv/DeviceType.kt` | `UiModeManager`, `FEATURE_LEANBACK`, low-RAM, and hardware feature detection. |
| **TV Theme & Tokens** | `app/src/main/java/com/music/bitchord/ui/tv/theme/TvTheme.kt` | TV Material 3 dark color scheme, 10-foot SF Pro typography, dimension tokens. |
| **TV Focus Primitives** | `app/src/main/java/com/music/bitchord/ui/tv/focus/TvFocus.kt` | Remote focus modifiers, scale animations, D-pad and media key handlers. |
| **TV UI Components** | `app/src/main/java/com/music/bitchord/ui/tv/components/TvComponents.kt` | `TvCard`, `TvButton`, `TvSlider`, `TvSectionHeader`, `TvDialog`, `TvSkeletons`, `TvErrorState`. |
| **TV Home Screen** | `app/src/main/java/com/music/bitchord/ui/tv/screens/TvHomeScreen.kt` | Horizontally scrolling shelves (Quick Picks, Recommended, Charts, Albums, Artists). |
| **TV Search Screen** | `app/src/main/java/com/music/bitchord/ui/tv/screens/TvSearchScreen.kt` | System TV IME search input, category filter chips (Songs, Albums, Artists, Playlists), results grid. |
| **TV Library Screen** | `app/src/main/java/com/music/bitchord/ui/tv/screens/TvLibraryScreen.kt` | Liked songs, downloads hub, local storage audio browser, history, saved playlists. |
| **TV Detail Screen** | `app/src/main/java/com/music/bitchord/ui/tv/screens/TvDetailScreen.kt` | 16:9 Landscape 2-pane album/playlist/artist view with tracklist and actions. |
| **TV Now Playing** | `app/src/main/java/com/music/bitchord/ui/tv/screens/TvNowPlayingScreen.kt` | 16:9 Landscape player, D-pad seekbar (±10s), synchronized lyrics, queue drawer, stats for nerds. |
| **TV Settings Screen** | `app/src/main/java/com/music/bitchord/ui/tv/screens/TvSettingsScreen.kt` | Audio quality, Crossfade, Automix, Lyrics, Scrobbling, Discord RPC, Cache, GPL info. |
| **TV Modal Dialogs** | `app/src/main/java/com/music/bitchord/ui/tv/dialogs/TvDialogs.kt` | Cookie sign-in, Discord token, Scrobble status, Source module settings, About dialog. |
| **TV Launcher Banner** | `app/src/main/res/drawable/tv_banner.xml` | 320x180 px 16:9 high-contrast launcher banner vector. |
| **TV Manifest Declarations** | `app/src/main/AndroidManifest.xml` | Leanback & optional hardware features, TV activity registration, banner tag. |

---

## 4. Security & Privacy Audit

1. **No Insecure QR / Token Relays**: All authentication remains strictly direct. Cookie tokens are stored in encrypted SharedPreferences (`EncryptedSharedPreferences` via `androidx.security.crypto`).
2. **No Sensitive Data Leaks**: Cookies, tokens, and stream URLs are never logged in production log output.
3. **Secure WebViews**: No unrestricted JavaScript interfaces or cross-origin allowances.
4. **License Preservation**: GNU General Public License v3.0 (GPLv3) notices and upstream author credits preserved in all headers and in-app About dialogs.

---

## 5. Performance & Resource Management

- **Low-RAM Safeguards**: Detects `ActivityManager.isLowRamDevice` to throttle GPU blur effects and reduce image cache allocations under memory pressure.
- **Image Scaling**: Image requests via Coil 3 specify exact target pixel dimensions to prevent decoding multi-megabyte bitmaps for small TV shelf cards.
- **Video Decoders**: Animated canvas decoders are limited to 1 instance at a time and released when backgrounded or navigated away from Now Playing.
