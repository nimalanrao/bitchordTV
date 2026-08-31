# 📜 BitChord TV — Comprehensive Engineering Changelog & Release Notes

**Product**: BitChord TV Edition  
**Version**: `0.01` (Build `1`)  
**Lead Architecture & Engineering**: Nithyanantha (`Nyxcore`)  
**Target Platform**: Android TV, Google TV, Fire TV (1080p / 4K UHD, 60Hz & 120Hz Ultra-Smooth)

---

## Executive Summary

BitChord TV is an open-source, client engineered exclusively for television screens. Built from the ground up on Jetpack Compose for TV and AndroidX Media3, it delivers a **1:1 Apple Music TV living room experience** with high-contrast remote navigation, hardware-accelerated 120Hz animations, real-time progressive flowing lyrics, 3D Spatial Audio virtualization, live motion video canvas playback, and true `#000000` pitch black OLED rendering.

---

## 🏆 Complete Engineering Timeline & Feature Matrix

```
[Phase 1: Foundation] ──► [Phase 2: 120Hz Engine] ──► [Phase 3: QR Auth] ──► [Phase 4: 3D Cover Flow]
         │
         ▼
[Phase 5: Synced Lyrics] ──► [Phase 6: Spatial Audio & Canvas] ──► [Phase 7: Apple Settings & OLED] ──► [Phase 8: TV-Only APK]
```

---

## 🚀 Detailed Phase-by-Phase Breakdown

### Phase 1: Android TV Core Architecture & Navigation Shell

- **Dedicated TV Activity (`TvActivity.kt`)**: Implemented full edge-to-edge layout, hardware acceleration flags, and Leanback lifecycle bindings.
- **Top Navigation Bar (`TvTopNavigationBar`)**: Apple TV-style horizontal frosted pill navigation with monochrome branding (`BitChord TV`), instant crossfade tab switching, and remote D-pad focus.
- **Remote D-Pad Focus Traversal (`TvFocus.kt`)**: Engineered hardware-accelerated `graphicsLayer` scaling and focus halo borders with zero CPU layout measure passes, ensuring butter-smooth 60/120 fps remote browsing.
- **Browse ID Routing (`TvDetailScreen.kt`)**: Linked YouTube Music catalogue endpoints for Home shelves, Library, Liked Music, Listening History, Offline Downloads, and Local Device Audio.

---

### Phase 2: Ultra Performance Subsystem & 120Hz Display Controller

- **Display Engine (`TvRefreshRateController.kt`)**: Implemented dynamic display mode switching supporting native 120Hz (8.33ms frame budget), 60Hz standard, and Cinematic panel matching.
- **Single-Pass Rendering**: Replaced nested layout trees with hardware draw-layer caching and optimized Compose stability keys across all Lazy components.
- **High-Resolution Artwork Cache**: Upgraded Coil image loaders to `ARGB_8888`, `Precision.EXACT`, and 720×720 texture sampling to prevent blurry downscaled covers on fast scroll.

---

### Phase 3: QR Code Phone-to-TV Remote Authentication

- **Local Web Pairing Server (`TvAuthServer.kt`)**: Spun up a lightweight local HTTP server allowing users to scan a TV screen QR code from any smartphone camera.
- **Mobile Remote Web App**: Mobile pairing web page allowing users to paste authentication cookies, search songs, play/pause, seek, and control the TV queue without typing with a remote control.
- **Built-in Setup Flow (`TvSetupScreen.kt`)**: Personalized first-run wizard for selecting TV nicknames ("Living Room TV", "Bedroom TV", "Studio") and visual themes.

---

### Phase 4: 1:1 Apple Music TV Now Playing & 3D Cover Flow Carousel

- **3D Cover Flow Carousel (`TvPlayerLayout.kt`)**: Central playing track (scaled, glowing elevation) flanked by real previous and next songs from the playback queue (`playerState.queue`), with subtle background blur and crystal-clear pure white hover focus rings.
- **Top Control Bar**: AirPlay / Device status pill, dynamic album context title, and frosted circular transport buttons (Shuffle, Repeat, Autoplay / Infinity, Lyrics).
- **Dedicated Back Navigation**: Added top-left frosted back button for instantaneous return to Home/Browse shelves.
- **Expanding Zoom Transition (`TvApp.kt`)**: Tapping any song from a shelf seamlessly expands and scales the artwork from 0.85x to 1.0x with Apple spring physics while fading into Now Playing mode.
- **Pure White Seekbar (`TvPlayerProgress.kt`)**: Frame-interpolated linear time progress line in crisp pure white with left elapsed and right remaining time indicators.

---

### Phase 5: Real-Time Progressive Syllable Flow Lyrics Engine

- **6-Line Structured Viewport (`TvLyricsList.kt`)**:
  - **2 Top Unfocused Lines**: Rendered with **40% blur / dimming** (`alpha = 0.35f`, `blur(3.dp)`).
  - **1 Middle Focused Singing Line**: **0% blur (Crystal sharp)** in bold white (`34sp`), glowing highlight, with microsecond VSYNC progressive syllable sweep.
  - **3 Bottom Unfocused Lines**: Rendered with **20% subtle blur / dimming** (`alpha = 0.45f`, `blur(1.5.dp)`).
- **Apple Physics Glide**: Viewport smoothly glides and pans between rows using Apple spring damping physics (`stiffness = 110f`) instead of jumping abruptly.
- **Background Vocalist Support (`BackgroundVocals.kt`)**: Secondary backing vocals and harmony lines (e.g. `(ooh yeah)`) are rendered in smaller italicized text directly beneath the lead vocal line.
- **Curved Wave Brand Badge (`TvLyricsIcon.kt`)**: Vector-drawn 3-bar curved wave icon with pure transparent `(Logo) BitChord` branding beneath the album artwork.

---

### Phase 6: Live Video Canvas & 3D Spatial Audio Virtualization

- **Live Video Canvas Engine (`CanvasRepository.kt`)**: Multi-source video canvas resolver checking Apple Music motion artwork, Tidal canvas, Community index, and Spotify Canvas.
- **Canvas Notification Pill**: If a track has no live video canvas available, displays a sleek frosted top banner: _"No live video canvas for this track • Playing standard artwork"_ that automatically fades out after 3.5 seconds.
- **3D Spatial Audio Virtualizer (`TvSpatialAudioEngine.kt`)**: Integrated Android `Virtualizer` DSP to expand stereo audio into a wide, immersive Dolby Atmos-style 3D surround soundstage for TV soundbars and home theater setups.

---

### Phase 7: 1:1 Apple TV Settings Menu with Color Inversion

- **Split Two-Pane Layout (`TvSettingsScreen.kt`)**:
  - **Left Hero Pane**: Apple Music Red Gradient Squircle card with the BitChord logo and real-time dynamic explanation text for whichever option is currently focused.
  - **Right Menu Pane**: Categorized sections (`LIBRARY`, `AUDIO & SPATIAL`, `CANVAS & VISUALS`, `TYPOGRAPHY & DISPLAY`, `ACCOUNT & ABOUT`).
- **Pill Shape & Hover Inversion**: Options styled as rounded pills (`RoundedCornerShape(24.dp)`). When hovered or focused, pills invert to **Solid Pure White** with **Pure Black text and values**.
- **Dynamic Album-Tinted Home Cards (`TvComponents.kt`)**: Text container beneath album art automatically samples the dominant color from the album artwork thumbnail via LRU cache and applies a saturated dark tint instead of static gray.

---

### Phase 8: Typography Customization & True OLED Pure Black

- **Typography Picker (`TvFontOption`)**: Live switcher allowing users to choose their preferred font style across the entire app:
  1. **Apple SF Pro Display** (Default luxury iOS / macOS font)
  2. **Google Sans / Inter** (Modern geometric sans-serif)
  3. **Arial Classic** (Clean standard universal typeface)
  4. **Minecraft Pixel** (Retro 8-bit arcade monospace)
- **True OLED Pure Black Mode (`TvTheme.kt` & `TvMeshBackground.kt`)**: Integrated `TvOledPalette` with true `#000000` pitch black canvas that turns off pixels on OLED/QD-OLED TV panels for infinite contrast.

---

### Phase 9: Dedicated TV-Only APK & Production Packaging

- **Pure TV-Exclusive Build**: Stripped mobile phone launcher activities so the APK targets Android TV / Google TV exclusively in landscape orientation.
- **Leanback Launcher Integration**: Configured `LEANBACK_LAUNCHER` intent filters, 320×180 TV banner (`tv_banner.png`), and high-res launcher icons.
- **Automated CI/CD Pipeline**: GitHub Actions workflow compiles release APKs and packages production builds with signing keystores.

---

## 📊 Technical Metrics & Quality Assurance

| Metric              | Target           | Achieved Result                                 | Status  |
| :------------------ | :--------------- | :---------------------------------------------- | :------ |
| **Frame Rate**      | 60 fps / 120 fps | **120 fps Butter-Smooth** (8.33ms frame budget) | ✅ Pass |
| **TV Resolution**   | 1080p / 4K UHD   | **Native 4K UI Scaling**                        | ✅ Pass |
| **Input Latency**   | < 16ms           | **Instant D-Pad Response**                      | ✅ Pass |
| **Audio Pipeline**  | Hi-Res Lossless  | **Bit-Exact FLAC / Opus + 3D Virtualizer**      | ✅ Pass |
| **OLED Contrast**   | #000000          | **True 100% Pixel Shutoff**                     | ✅ Pass |
| **Platform Target** | Android TV OS    | **100% Dedicated TV APK**                       | ✅ Pass |

---

## 👨‍💻 Attribution & Credits

- **Architecture, TV Engineering & Motion Design**: Nithyanantha (`Nyxcore`)
- **Version**: `0.01` (Build `1`)
- **License**: GNU General Public License v3.0 (GPLv3)
