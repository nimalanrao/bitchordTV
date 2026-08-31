# BitChord TV Implementation Report

**Version:** 0.01 (Build 1)  
**TV Platform Lead & UX Architect:** Nyxcore  
**Base Application:** BitChord (Kushagrasinghx)  
**License:** GNU General Public License v3.0 (GPLv3)

---

## 1. Executive Summary

BitChord has been transformed into a universal Android application supporting both handheld devices (smartphones, tablets) and Android TV / Google TV / Fire OS devices from a single unified codebase and application package.

The mobile experience (`MainActivity`, mobile Material 3, touch navigation, widgets, mini-player bottom sheets) remains 100% intact and untouched. The Android TV experience (`TvActivity`, TV Material 3, 10-foot remote-first D-pad navigation, 16:9 two-pane layouts, and cinematic player with real-time synchronized lyrics) provides a first-class television listening environment engineered by **Nyxcore**.

---

## 2. Architecture & Design Decisions

### 2.1 Single-Package Universal Architecture
- **Single Package (`com.music.bitchord`)**: Avoids duplicate codebase maintenance, enables smooth shared storage/downloads, and maintains unified update channels.
- **Dual Launcher Entries**:
  - `MainActivity`: Registered with `android.intent.action.MAIN` + `android.intent.category.LAUNCHER` (portrait, mobile theme).
  - `TvActivity`: Registered with `android.intent.action.MAIN` + `android.intent.category.LEANBACK_LAUNCHER` (landscape, TV theme, singleTask).
- **Dedicated TV Launcher Channel**: Includes 320x180 px bitmap PNG banners across density qualifiers so the app is placed directly in the main TV **"Apps"** home row rather than hidden under Settings.
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
  - Compose Foundation `LazyRow` and `LazyColumn` for horizontal shelves and vertical catalogs.
- **Cinematic Player & Synchronized Lyrics (`com.music.bitchord.ui.tv.player.*`)**:
  - Full-bleed 16:9 artwork/canvas background with top and bottom contrast scrims.
  - Real-time word-by-word and line-by-line synchronized lyrics engine with binary search indexing.
  - D-pad Up/Down manual lyric browse mode with floating "Return to current line" action.
  - D-pad Seekbar with ±10s stepping and acceleration.
  - Large circular play/pause anchor button flanked by full transport controls and Show/Hide lyrics pill.
  - Smart auto-hide controls during uninterrupted playback.

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
| **Cinematic Player Orchestrator** | `app/src/main/java/com/music/bitchord/ui/tv/player/TvNowPlayingScreen.kt` | Full-bleed cinematic player and synchronized lyrics layout manager. |
| **Cinematic Player Layout** | `app/src/main/java/com/music/bitchord/ui/tv/player/TvPlayerLayout.kt` | 16:9 visual hierarchy with context label, metadata row, timeline, and transport controls. |
| **TV Player Controls** | `app/src/main/java/com/music/bitchord/ui/tv/player/TvPlayerControls.kt` | Transport row with favorite, shuffle, previous, large play/pause anchor, next, queue, and lyrics pill. |
| **TV Player Progress** | `app/src/main/java/com/music/bitchord/ui/tv/player/TvPlayerProgress.kt` | Thin timeline with D-pad seek controls (±10s) and timestamps. |
| **TV Player Background** | `app/src/main/java/com/music/bitchord/ui/tv/player/TvPlayerBackground.kt` | Full-bleed background with dynamic crossfade and cinematic darkening/contrast scrims. |
| **Synchronized Lyrics Engine** | `app/src/main/java/com/music/bitchord/ui/tv/player/LyricsSynchronizer.kt` | Millisecond-accurate binary search and word-level progress calculation. |
| **TV Lyrics Overlay** | `app/src/main/java/com/music/bitchord/ui/tv/player/TvLyricsOverlay.kt` | 2-column split lyrics layout (38% metadata & mini controls, 62% lyrics). |
| **TV Lyrics List** | `app/src/main/java/com/music/bitchord/ui/tv/player/TvLyricsList.kt` | Synchronized scrolling list with active line emphasis, word sync, and manual browse mode. |
| **TV Settings Screen** | `app/src/main/java/com/music/bitchord/ui/tv/screens/TvSettingsScreen.kt` | Audio quality, Crossfade, Automix, Lyrics, Scrobbling, Discord RPC, Cache, GPL & Nyxcore info. |
| **TV Modal Dialogs** | `app/src/main/java/com/music/bitchord/ui/tv/dialogs/TvDialogs.kt` | Cookie sign-in, Discord token, Scrobble status, Source module settings, About dialog. |
| **TV Launcher Banners** | `app/src/main/res/drawable-*/tv_banner.png` | 320x180 px 16:9 high-contrast launcher banner bitmaps for all screen densities. |
| **TV Manifest Declarations** | `app/src/main/AndroidManifest.xml` | Leanback & optional hardware features, TV activity registration, banner tag. |

---

## 4. Credits & Authorship

- **Nyxcore**: TV Platform Lead, Android TV / Google TV Architecture, Remote Focus Engine, UI/UX Design, and TV Synchronized Lyrics implementation.
- **Kushagra Singh**: Original BitChord mobile application creator.
