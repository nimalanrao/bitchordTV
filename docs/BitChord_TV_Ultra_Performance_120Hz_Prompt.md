/goal

# Make BitChord’s TV player ultra-smooth, benchmarked, and capable of an opt-in 120 Hz UI mode

Work directly in the currently open BitChord Android repository. This goal follows the TV Now Playing and lyrics implementation and is limited to **performance, frame pacing, memory, rendering, player responsiveness, and a TV refresh-rate setting**. Do not redesign the player, rewrite unrelated screens, or weaken playback features. Preserve the cinematic TV player, synchronized lyrics, phone UI, Media3 playback service, lossless audio, crossfade, Automix, queue, downloads, and existing behavior.

Use Gemini 3.7 Flash with High thinking if available. Inspect and profile the real project, make the changes, run release/profileable benchmarks and tests, repair regressions, and leave evidence. Do not stop at recommendations or claim “optimized” from code inspection. Do not narrate hidden chain-of-thought.

## Required outcome

Deliver a TV player that:

1. Feels immediate and fluid on ordinary 60 Hz Android TV / Google TV hardware.
2. Offers **Settings → Playback/Display → Interface refresh rate** with:
   - **Auto (Recommended)**
   - **Smooth 60 Hz**
   - **Ultra 120 Hz**
3. Enables Ultra 120 Hz only when the active TV display exposes a compatible approximately-120 Hz mode at the current physical resolution.
4. Requests—never falsely guarantees—up to 120 fps UI rendering using the correct Android window/display and Compose frame-rate APIs available to the selected SDK/dependency versions.
5. Keeps animated canvas/video at its real source frame rate, such as 24, 25, 29.97, or 30 fps. Do not interpolate it to fake 120 fps.
6. Meets the 8.33 ms frame budget for sustained interactive UI motion on tested 120 Hz hardware, with evidence from frame traces/benchmarks.
7. Meets the 16.67 ms frame budget on tested 60 Hz hardware.
8. Keeps audio stable: no underruns, clicks, dropouts, source timeouts, MediaSession lag, or crossfade/Automix regression caused by UI optimization.
9. Uses less memory and GPU work, not merely faster-looking animations.
10. Falls back safely on 50/60 Hz, low-RAM, thermally constrained, unsupported, or buggy TV devices.

## Truth about “120 fps on TV”

Implement and document these platform realities:

- A TV must advertise a 120 Hz display mode to Android. Marketing claims on the panel or HDMI port are not enough.
- The Android device, TV panel, current resolution, HDMI chain where applicable, firmware, and system mode must all support it.
- `Surface.setFrameRate`, a window refresh preference, Compose frame-rate voting, and `preferredDisplayModeId` are requests/hints. Android may decline or choose a multiple for system, power, thermal, competing-surface, or mode-switch reasons.
- Some TV mode changes are non-seamless and can show a black screen for one or two seconds.
- 120 Hz is the display refresh rate. The app can render UI updates up to that rate only while something is changing and only if it finishes frames within roughly 8.33 ms.
- Static music-player UI should not redraw 120 times per second. A static screen rendering zero new frames is more optimized than a pointless 120 fps loop.
- A 24 fps video can display evenly on 120 Hz using 5:5 cadence; a 30 fps video can use 4:4 cadence. Keep source timestamps accurate.
- An emulator cannot prove a physical television actually outputs 120 Hz. Final 120 Hz verification requires compatible physical hardware and actual-mode evidence.

Never label the setting as guaranteed “120 FPS.” Use **Ultra 120 Hz** with helper text such as “Requests up to 120 fps for interface motion on compatible TVs.” Show the actual active refresh rate separately when known.

## Official sources of truth

Refresh current details before implementing and prefer official Android/AndroidX/Media3 documentation over blogs or copied snippets:

- Android frame-rate API and TV switching behavior: `https://developer.android.com/media/optimize/performance/frame-rate`
- Adaptive refresh rate and Compose frame-rate voting: `https://developer.android.com/develop/ui/views/animations/adaptive-refresh-rate`
- `Display.Mode`: `https://developer.android.com/reference/android/view/Display.Mode`
- `Display.getSupportedModes()`: `https://developer.android.com/reference/android/view/Display#getSupportedModes()`
- `WindowManager.LayoutParams`: `https://developer.android.com/reference/android/view/WindowManager.LayoutParams`
- Compose `FrameRateCategory`: `https://developer.android.com/reference/kotlin/androidx/compose/ui/FrameRateCategory`
- Compose performance: `https://developer.android.com/develop/ui/compose/performance`
- Compose performance best practices: `https://developer.android.com/develop/ui/compose/performance/bestpractices`
- Compose stability diagnosis/fixes: `https://developer.android.com/develop/ui/compose/performance/stability/diagnose`
- Android TV memory optimization: `https://developer.android.com/training/tv/playback/memory`
- JankStats: `https://developer.android.com/topic/performance/jankstats`
- Macrobenchmark: `https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview`
- Macrobenchmark metrics: `https://developer.android.com/topic/performance/benchmarking/macrobenchmark-metrics`
- Baseline Profiles: `https://developer.android.com/topic/performance/baselineprofiles/create-baselineprofile`
- Measuring Baseline Profiles: `https://developer.android.com/topic/performance/baselineprofiles/measure-baselineprofile`
- Media3 frame-rate strategy: `https://developer.android.com/reference/androidx/media3/common/C.VideoChangeFrameRateStrategy`
- Media3 ExoPlayer customization: `https://developer.android.com/media/media3/exoplayer/customization`

Verify exact API names against the versions actually compiled. For example, Compose frame-rate modifiers require a sufficiently current Compose UI version, while adaptive refresh-rate support depends on newer Android versions and display HAL support. Do not paste an API that does not exist in the repository’s selected version.

## Phase 0 — Establish an honest performance baseline

Before editing:

1. Record branch, HEAD, `git status`, Gradle/AGP/Kotlin/Compose/Media3 versions, target SDK, device API, TV model, display modes, resolution, active refresh rate, RAM class, `isLowRamDevice`, and build variant.
2. Preserve all existing unrelated changes.
3. Build and measure a **release or profileable non-debuggable-equivalent** variant. Compose debug performance is not representative.
4. Enable/use R8 for release measurement exactly as production does.
5. Create a repeatable 30–60 second critical user journey:
   - open TV Now Playing;
   - reveal/hide controls;
   - traverse every control repeatedly by D-pad;
   - hold-seek and release;
   - open/close lyrics;
   - render word-synced lyrics during playback;
   - manually scroll and return to current line;
   - rapidly skip tracks five times;
   - transition canvas/artwork and palette;
   - pause/resume and return from background.
6. Capture baseline `FrameTimingMetric`, JankStats state-tagged results, Perfetto/FrameTimeline trace, memory, GC events, CPU/GPU load where available, image allocation, decoder count, dropped canvas frames, audio underruns, and startup timings.
7. Identify the real top offenders from traces. Do not blindly apply every theoretical optimization.

Create `docs/TV_PERFORMANCE_REPORT.md` containing the baseline device/build/method, actual metrics, trace references, bottlenecks, changes, and final comparison. Separate measured facts from expectations.

## Phase 1 — Refresh-rate domain model and capability detection

Create a small lifecycle-safe subsystem rather than scattering display calls through composables. Adapt names to repository conventions:

```kotlin
enum class TvRefreshRatePreference {
    SYSTEM_AUTO,
    SMOOTH_60,
    ULTRA_120
}

data class TvDisplayCapabilities(
    val displayId: Int,
    val currentModeId: Int,
    val currentPhysicalWidth: Int,
    val currentPhysicalHeight: Int,
    val actualRefreshRateHz: Float,
    val supportedModes: List<TvDisplayMode>,
    val compatible60ModeId: Int?,
    val compatible120ModeId: Int?,
    val canSwitchTo120Seamlessly: Boolean,
    val hasAdaptiveRefreshRateSupport: Boolean?
)
```

Implement equivalent classes such as `TvDisplayCapabilitiesReader` and `TvRefreshRateController` outside the UI.

Capability rules:

- Read the activity’s actual display and `Display.supportedModes`/`Display.mode` through APIs valid for the project’s min/target SDK.
- Treat common fractional rates correctly: approximately 59.94 as 60 and approximately 119.88 as 120. Use a narrow documented tolerance, not integer equality.
- A 120 candidate must be around 120 Hz and match the current mode’s physical width and height. Do not silently lower 4K to 1080p merely to expose 120 Hz.
- If several candidates exist, prefer the closest rate, identical resolution, a seamless alternative where available, and compatible HDR/color behavior where the platform exposes it.
- `Display.Mode.alternativeRefreshRates` is available only on newer API levels and indicates that a possible switch is seamless; its presence does not guarantee Android will switch.
- Do not use deprecated `getSupportedRefreshRates()` when `supportedModes` provides the required resolution-aware data.
- On supported newer APIs, read adaptive-refresh capability; on older APIs represent it as unknown rather than false.
- Listen through `DisplayManager.DisplayListener` for active-mode/display changes. Register and unregister with the TV activity lifecycle.
- Recompute capabilities when the activity moves to another display, the display mode list changes, or the TV/HDMI route changes.
- Do not poll display modes every frame or every recomposition.
- Unit-test the selector with 60-only, 50/60, 1080p120, 4K60 + 1080p120, 4K120, fractional 59.94/119.88, seamless/non-seamless, duplicate, and empty mode lists.

## Phase 2 — Settings experience

Add a TV-remote-friendly setting under the appropriate Display, Appearance, or Playback section, reusing the existing settings store/DataStore architecture.

### Auto (Recommended)

- Clear any explicit window display-mode/refresh preference.
- Let Android choose the display mode.
- On supported Compose/ARR versions, use default frame-rate behavior and request a high category only for composables with meaningful high-motion animation when measurement proves it useful.
- This must be the default for new and existing users unless a prior setting is migrated.

### Smooth 60 Hz

- Request a same-resolution approximately-60 Hz mode/rate when available.
- If current hardware has no compatible 60 mode, disable the option or fall back to Auto with a clear explanation.
- Never change resolution automatically.

### Ultra 120 Hz

- Display only as enabled when a same-resolution approximately-120 Hz candidate exists.
- If unsupported, show it disabled with text such as “This TV does not report a compatible 120 Hz mode at the current resolution.”
- Description: “Requests up to 120 fps for interface motion. Canvas video keeps its original frame rate.”
- Show a small current-state label: `Active: 119.88 Hz`, `Requested: 120 Hz · Active: 60 Hz`, or `System controlled`, based on real observation.
- Never show “120 Hz active” merely because the user selected it.

For a non-seamless explicit mode change:

1. Warn once that the screen may briefly go black.
2. Apply the mode to the TV activity window.
3. Wait for a real display-change callback with a bounded timeout.
4. Show a remote-focused **Keep 120 Hz?** confirmation with a 15-second rollback timer.
5. Persist Ultra 120 Hz only after confirmation. If the app pauses, crashes/restarts before confirmation, times out, loses the display, or cannot observe a compatible active mode, restore Auto/previous mode safely.
6. Do not repeat confirmation on every launch for the same confirmed display capability unless the display identity/mode set materially changes.

All dialogs must be fully D-pad operable, with safe default focus on rollback/cancel where appropriate.

Store the user preference, confirmation state, and capability fingerprint without storing fragile mode IDs as eternal truth. Re-select the correct mode from current capabilities on each relevant launch because OEM mode IDs can change.

## Phase 3 — Correct platform requests

Implement the selected policy once per activity/display/preference change, never per recomposition or animation frame.

### Window mode

- On Android TV, an explicit 120 Hz user choice may use `WindowManager.LayoutParams.preferredDisplayModeId` because official Android guidance recognizes it for TV cases where a heavy/non-seamless mode switch is desired.
- Select only the validated same-resolution mode ID from current `Display.Mode` data.
- For Auto, set the mode ID to the platform default value and clear explicit refresh preferences.
- If only the refresh rate should be hinted and a heavy switch is not intended, prefer the appropriate refresh-rate API rather than needlessly forcing a display mode.
- Apply window attributes only when the effective request changes.
- Do not modify global Android settings, use root/ADB commands in production, change resolution, enable game mode, or force another app’s display behavior.

### Compose UI frame-rate request

- If the selected compatible Compose UI version provides `Modifier.preferredFrameRate(...)`/`FrameRateCategory`, use the exact stable API verified by compilation.
- Ultra 120: request `120f` or a high category for the TV player root/actively animating regions as appropriate.
- Smooth 60: request `60f`/normal behavior as appropriate.
- Auto: use `FrameRateCategory.Default`, removing app preference when static.
- Compose 1.9 introduced adaptive refresh-rate support; if the repository is older, evaluate a minimal compatible update. Do not perform a blind dependency upgrade. If upgrading, run the entire phone/TV build and test suite.
- On platforms where the Compose modifier or adaptive refresh rate is unsupported, the screen must compile and work through guarded fallback behavior.
- Do not attach 120 Hz votes to invisible/offscreen composables.

### Animated canvas/video surface

- Do **not** call `setFrameRate(120)` on a 24/30 fps canvas surface.
- When the real canvas frame rate is known and API support exists, report the exact source rate to its Surface/Media3 strategy using fixed-source semantics as official guidance requires.
- Use a seamless-only strategy for short music canvases by default. Do not use `CHANGE_FRAME_RATE_ALWAYS` for short loops and cause repeated black screens.
- Clear the surface frame-rate hint when the surface remains visible but playback stops; destroying/hiding the surface follows platform lifecycle behavior.
- If the selected window runs at 120 Hz, 24/30 fps content can still be presented evenly while UI motion renders at up to 120 Hz.
- Do not enable Media3 asynchronous codec queueing everywhere merely because the API exists. It is default on newer Android versions; enable it on older/problem devices only when measured dropped frames/audio underruns justify it.

## Phase 4 — Recomposition and state-flow surgery

Profile the current TV player and fix measured invalidation scope.

- Split slow state—metadata, artwork URI, favorite, shuffle, queue capability, lyrics content—from fast state—position, active lyric token fraction, focus animation.
- A position update must not recompose the full-screen background, metadata, every control, and all lyric lines.
- Keep authoritative playback events in the MediaController/Player listener. Project current position from a stable base position, event time, playback speed, play/buffer state, and `withFrameNanos` only while a visible element needs per-frame motion.
- Suspend frame callbacks when paused, buffering, ended, activity not started, lyrics/control progress not visible, or animations are idle.
- Use lower-frequency updates for text timestamps where per-frame precision is invisible; draw the thin progress bar smoothly in a narrow draw scope.
- Word-synced lyrics may update active fill at vsync, but only the active line/token drawing layer should invalidate. Do not rebuild or remeasure the whole lyrics list 120 times per second.
- Locate active lines/words by binary search/index cursor. Never scan all tokens per frame.
- Use `derivedStateOf` only to reduce genuinely noisy downstream state, not as decoration everywhere.
- Defer frequently changing reads to layout/draw lambdas where appropriate. Prefer `graphicsLayer {}`/draw-phase properties for focus scale/alpha instead of changing layout dimensions.
- Use stable item keys, immutable state, event lambdas, and compiler-inferred stability. Inspect Compose compiler stability reports for measured hotspots.
- Strong skipping is already enabled by default on sufficiently current Kotlin versions; verify rather than duplicating flags.
- Never add `@Stable` or `@Immutable` to mutable classes just to silence a report. Those annotations are contracts.
- Move sorting, lyric parsing, provider matching, palette extraction, bitmap analysis, and expensive formatting outside composable bodies and off the main thread.
- Cache `Brush`, gradient, path, text/style, time formatter, icon vector, and other repeated objects with correct keys.
- Avoid backwards state writes and cascaded `LaunchedEffect` loops.

Use Layout Inspector/recomposition counts and Perfetto trace sections to prove the full player root no longer recomposes for every progress/word frame.

## Phase 5 — Rendering and GPU optimization

Preserve the cinematic design while reducing render cost:

- Combine static darkening/scrim gradients into as few draw passes as practical.
- Use `drawWithCache` for gradients/paths that depend only on size, palette, or stable theme values.
- Avoid stacking multiple full-screen alpha, blur, RenderEffect, clipping, and offscreen-compositing layers.
- Avoid full-screen blur on low-RAM devices and in Ultra 120 mode unless benchmarks prove the frame budget. Use a precomputed/downsampled blur or optimized translucent gradient fallback.
- Avoid animating blur radius, large shadows, or full-screen color matrices every frame.
- Apply focus scale in a render layer so neighboring controls do not remeasure/re-layout.
- Keep focus animation short and avoid spring overshoot that creates long frame work.
- Decode animated canvas through hardware acceleration when supported and retain at most one video decoder.
- Prefer the surface type that benchmarks best for the existing layering requirements. A `SurfaceView` may reduce composition cost, but do not switch from `TextureView` blindly if clipping/transforms/stacking break; measure both if architecture permits.
- Keep canvas source resolution appropriate for the display/video region. Do not decode 4K video for a low-resolution source or upscaled UI.
- Do not allocate bitmaps, gradients, paths, text layouts, arrays, or lists inside per-frame callbacks.
- Check GPU overdraw and eliminate invisible full-screen layers.
- Avoid forcing software rendering.

When Ultra 120 is active, preserve visual identity but automatically select optimized equivalents for expensive effects: static/precomputed background blur, simpler shadow, no redundant mesh animation, restrained canvas overlays, and bounded focus effects.

## Phase 6 — Artwork and image pipeline

- Request images at the actual rendered pixel size, not original 4000×4000 artwork by default.
- TV application UI is often rendered at 720p or 1080p even when video output is 4K; read actual window/UI bounds.
- Reuse disk/source cache while avoiding duplicate full-size memory bitmaps for background, thumbnail, and palette extraction.
- Generate palette from a small downsampled bitmap off the main thread and cache by stable artwork identity.
- Cancel stale image/palette jobs during rapid Next presses.
- Crossfade only fully decoded current/next assets and avoid holding several full-resolution previous backgrounds.
- Use a lightweight fallback immediately so track changes never flash white/black or stall focus.
- Respect low-memory callbacks and trim image/video caches appropriately.

## Phase 7 — Lyrics performance at 60/120 Hz

- Keep parsed lyrics immutable and cached per track.
- Keep only a small visible window of nearby lines in the active TV lyric layout where design permits.
- Precompute normalized timing and line/token indices once per result.
- Do not create a Flow per word or launch one coroutine per line/token.
- Only the active word fill and necessary scroll position animate at frame rate.
- Use one synchronization clock tied to authoritative player state.
- Manual scrolling pauses auto-follow work; hidden lyrics stop all lyric draw-loop work while preserving cached content.
- Seek/track change updates the current index immediately without animating through every skipped line.
- Text measurement/layout should be reused until content, width, style, font scale, or locale changes.
- Avoid marquee and lyric auto-scroll competing simultaneously.
- TalkBack must not receive announcements every frame/word.

Benchmark word-heavy songs, long unsynced lyrics, missing lyrics, rapid seeking, 0.5× and 2× speed, and track transitions.

## Phase 8 — Audio and analyzer isolation

UI smoothness must never steal time from audio:

- Never parse lyrics, extract palettes, allocate images, run visualizer FFT, or execute Compose work on ExoPlayer/audio callback threads.
- Inspect `VisualizerAudioProcessor`/native analyzer behavior. Gate PCM/FFT work when the visualizer is invisible, the TV activity is backgrounded, or low-RAM/performance policy disables it.
- Compute audio analysis at the minimum rate needed for the visual result, such as 30–60 updates per second, then interpolate lightweight visual values at display refresh. Do not run expensive FFT 120 times per second.
- Reuse primitive buffers and avoid per-buffer allocations/copies.
- Preserve audio focus, crossfade, Automix, lossless decoding, speed, skip silence, gapless behavior, and MediaSession responsiveness.
- Measure audio underruns before/after using Media3 analytics/logging available in the selected version.
- Do not arbitrarily change thread priorities, buffer sizes, or decoder flags without measured evidence and regression tests.

## Phase 9 — Memory and lifecycle

Follow current Android TV memory guidance:

- Detect `ActivityManager.isLowRamDevice`; do not use a brand/model blacklist.
- On roughly 1 GB low-RAM TVs, aim below 280 MB total application memory and preferably below 200 MB anonymous + graphics memory during normal player/lyrics use. Record measured values.
- At most one canvas decoder and one authoritative audio player.
- Release canvas video, high-resolution background, focus-only graphics, benchmark/JankStats listeners, and display listeners at the correct lifecycle boundary.
- Keep the MediaSession audio service alive independently when playback continues.
- No leaked Activity, ComposeView, MediaController future, Surface, Bitmap, WebView, listener, coroutine scope, or native analyzer buffer.
- Handle `onTrimMemory`, activity recreation, Ambient Mode/screensaver, Home/background, display reconnect, and process recreation.
- Ultra 120 preference must survive recreation without repeatedly switching modes or looping confirmation.
- Stop per-frame clocks when the screen is not visible.

## Phase 10 — Build/runtime optimization

- Measure release mode with production R8 optimization and resource shrinking configuration.
- Audit R8 keep rules. Remove only unjustified broad rules after verifying reflection/serialization/JNI behavior; never break modules or Media3.
- Generate an app-specific Baseline Profile covering TV launch to Now Playing, player controls, lyrics open/close, D-pad seeking, and a track transition.
- Include Startup Profile paths where appropriate.
- Verify the profile is packaged in APK/AAB and installed for benchmark measurement.
- Compare compilation modes using Macrobenchmark; do not quote generic 30% improvements as this app’s result.
- Add narrow trace sections around lyrics lookup, palette extraction, canvas transition, focus animation setup, and player-position projection where useful.
- Keep StrictMode/debug diagnostics out of release behavior while using them during development to find main-thread disk/network work.
- Avoid sweeping dependency updates. Upgrade Compose only when required for verified frame-rate/performance APIs and after full compatibility testing.

## Phase 11 — Benchmark and proof matrix

Create/update a benchmark module using official Macrobenchmark and Baseline Profile tooling.

Required metrics where supported:

- `FrameTimingMetric` including frame duration/overrun percentiles.
- `StartupTimingMetric` for cold/warm player launch.
- JankStats with UI state labels: `PlayerEnter`, `ControlsFocus`, `Seek`, `LyricsOpen`, `LyricsWordSync`, `LyricsScroll`, `TrackChange`, `CanvasTransition`, `BackgroundReturn`.
- Memory before/during/after canvas and lyrics.
- GC count/pause evidence.
- Canvas dropped frames and decoder initialization.
- Audio underruns.
- Active display mode/refresh rate observed during the run.

Test at least:

1. 60 Hz ordinary Android TV/Google TV hardware.
2. A low-RAM or constrained TV profile.
3. A real 120 Hz-capable Android TV/Google TV device at a same-resolution mode.
4. Auto, Smooth 60, and Ultra 120 on the 120 Hz device.
5. Unsupported 120 option on a 60-only device.
6. Fractional 59.94/119.88 modes.
7. Mode change, confirmation, timeout rollback, app background/return, activity recreation, and display reconnect.
8. 720p, 1080p, and 4K output where hardware permits.
9. Canvas hidden/static, 24 fps, 30 fps, missing/error, and rapid track changes.
10. Line- and word-synced lyrics, manual scroll, seek, pause, speed change, and long track.

Acceptance targets on the tested reference hardware after warmup:

- For real active 120 Hz during sustained UI interaction, target a p95 frame time within the approximately 8.33 ms budget and less than 1% janky frames for the standardized player journey. Record p50/p90/p95/p99 and every severe spike.
- For real active 60 Hz, target p95 within approximately 16.67 ms and less than 1% janky frames for the same journey.
- Zero frozen frames, ANRs, crashes, focus loss, audio underruns caused by the UI, or canvas decoder leaks.
- No material startup regression; report exact before/after rather than choosing an arbitrary device-independent millisecond limit.
- If hardware cannot meet a target, keep the safe fallback, identify the trace bottleneck, and do not claim the gate passed.

Use enough iterations to reduce noise, keep the device temperature/state controlled, and compare the same build/data/journey. Do not benchmark a debug build. Do not compare two different TVs as if they were an optimization A/B test.

Useful verification includes official Perfetto/Android Studio traces and appropriate read-only ADB diagnostics such as display mode, frame stats, process memory, and Media3 events. Never place ADB/root behavior in production code.

## Required tests

Add unit/instrumentation tests for:

- Mode capability parsing and same-resolution 60/120 selection.
- Fractional-rate tolerance.
- No resolution downgrade.
- Auto clears explicit mode preferences.
- Unsupported Ultra 120 is disabled.
- Preference migration and persistence.
- Non-seamless confirmation, success, timeout, cancellation, crash/restart-safe rollback, and changed-display fingerprint.
- Display listener lifecycle and no repeated apply loop.
- Actual vs requested refresh-rate labels.
- Compose fallback on older API/dependency support.
- Canvas source fps is not replaced with 120.
- Frame clock starts/stops for play, pause, buffer, hidden controls, hidden lyrics, background, and lifecycle changes.
- Fast progress state does not invalidate the full player tree; use measurable recomposition counters in test/debug code where reasonable.
- Lyrics sync remains correct at 60 and 120 display refresh, including speed and seek.
- Phone activity and phone settings remain unaffected unless the setting is intentionally shared and clearly scoped.

Run actual available tasks, adapting variant names:

```bash
./gradlew test
./gradlew assembleDevDebug
./gradlew lintDevDebug
./gradlew assembleProdRelease
./gradlew bundleProdRelease
./gradlew connectedDevDebugAndroidTest
./gradlew generateBaselineProfile
```

Run Macrobenchmarks on a profileable release-equivalent build and physical devices. If a named task differs, discover the real Gradle task. Never disable a failing test or lint rule just to produce green output.

## Prohibited shortcuts

- No permanent 120 Hz busy loop.
- No `delay(8)` polling loop pretending to be 120 fps.
- No global `preferredRefreshRate = 120f` without capability handling and lifecycle cleanup.
- No `Surface.setFrameRate(120)` on 24/30 fps video.
- No lower-resolution 120 mode selected without explicit separate user consent; this goal requires same-resolution compatibility.
- No hidden root, ADB, secure-settings, or OEM-specific hacks.
- No hard-coded TV model allowlist/denylist as the main capability system.
- No fake “120 active” badge based on preference alone.
- No expensive visualizer FFT at 120 Hz.
- No disabling canvas, lyrics, animations, or visual quality everywhere and calling that optimization.
- No removing accessibility semantics to save frames.
- No second audio player or duplicate MediaController.
- No benchmark claims from emulator/debug/one iteration.
- No made-up metrics, device results, or “100% smooth” claim without trace evidence.

## Definition of done

Complete only when:

- Auto, Smooth 60, and Ultra 120 settings exist and are fully D-pad operable.
- Ultra 120 is capability-gated to same-resolution advertised modes.
- Requested and actual refresh rates are distinguished.
- Non-seamless switching has warning, confirmation, timeout, and safe rollback.
- Mode requests are lifecycle-safe and not repeatedly applied.
- Compose/UI motion can request high refresh through APIs actually supported by the selected versions.
- Canvas video retains real source frame rate and correct presentation.
- The main player is no longer recomposed by every progress/word tick.
- Lyrics, progress, focus, canvas, artwork, palette, and analyzer paths meet measured frame/memory budgets on tested hardware.
- Static/paused/background states stop unnecessary frame work.
- 60-only and low-RAM TVs remain fast and correct.
- Audio playback and all existing player features pass regression testing.
- Baseline Profile is generated, packaged, and benchmarked.
- Release/profileable frame metrics, trace evidence, memory, actual mode, and device details are documented honestly.
- Builds, unit tests, lint, available instrumentation tests, and release artifacts succeed, or exact external blockers are reported.

## Final response contract

After implementation, give a concise handoff:

1. Performance architecture and the largest real bottlenecks fixed.
2. Auto/60/120 setting behavior and fallback rules.
3. Exact device, resolution, requested rate, actual active rate, API, build variant, and test journey.
4. Before/after frame p50/p90/p95/p99, jank percentage, startup, memory, decoder, GC, and audio-underrun results actually measured.
5. Baseline Profile/build/test results.
6. Unverified hardware or external blockers.

Never say “120 fps works” unless the physical display actually entered approximately 120 Hz and the measured UI met the frame budget. Start now with repository inspection and a release/profileable baseline, then implement, benchmark, repair, and document the result end-to-end.
