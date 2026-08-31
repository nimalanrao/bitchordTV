/goal

# Build BitChord’s production-grade TV Now Playing screen and fully working synchronized lyrics

Work directly in the currently open BitChord Android repository. This goal is deliberately limited to the **TV music player / Now Playing experience and lyrics**. Do not redesign Home, Search, Library, Settings, onboarding, downloads, or the phone UI. If a TV activity/navigation shell already exists, integrate into it. If it does not exist, create only the smallest host/route required to launch and test the TV player; do not expand into a full TV-app rewrite.

Use Gemini 3.7 Flash with High thinking if available. Inspect the real code before editing, implement the changes, run tests, fix problems caused by the work, and finish with verified code. Do not stop after a plan, mockup, or code snippets. Do not narrate private chain-of-thought.

## Core result

Create a premium, cinematic, remote-first TV player based on the supplied reference image `25fde7c9-1e66-403b-af19-4fc9972e65e3.png`.

The reference establishes the visual composition:

- Full-bleed 16:9 animated artist/album canvas or artwork background.
- Very dark cinematic treatment with subtle artwork colors still visible.
- Small source/context label in the upper-left.
- Song artwork, large title, artist/album metadata, and a thin progress line across the lower part of the screen.
- Playback times below the progress line.
- A centered transport-control row near the bottom.
- Favorite and shuffle actions to the left of transport controls.
- Previous, large circular play/pause, and next controls in the center.
- Queue/device-style secondary action after Next.
- A rounded **Show lyrics** button on the bottom-right.
- Extremely clean presentation with no mobile app bar, bottom navigation, phone bottom sheet, oversized cards, or unnecessary panels.

Do not copy Spotify’s logo, name, trademark, or exact proprietary assets. Use BitChord’s icon, typography, colors, artwork, and visual identity while reproducing the reference’s cinematic layout and interaction quality.

The final screen must look intentional at 720p, 1080p, and 4K, work completely with a five-way D-pad remote, control the existing Media3 playback session correctly, and provide reliable line-synced and word/syllable-synced lyrics.

## Scope boundaries

Only modify code needed for:

1. The TV Now Playing/player screen.
2. TV player controls and focus/input behavior.
3. Animated canvas/background artwork used by the TV player.
4. Lyrics fetching, parsing, synchronization, caching, rendering, and TV interaction.
5. Shared player/lyrics state required by the TV screen.
6. Focused tests, performance safeguards, and documentation for this feature.

Preserve the phone player exactly unless a small extraction is required to share non-UI logic. Do not replace the existing `PlaybackService`, repositories, database, source resolver, queue, downloads, crossfade, lossless playback, or Automix architecture. Reuse them.

## Inspect before changing anything

First inspect and record:

- Current Git branch, HEAD, and working-tree changes. Preserve unrelated and pre-existing edits.
- The existing TV activity/navigation code, if any.
- `NowPlayingScreen.kt`, `MainActivity.kt`, `PlaybackService.kt`, player/controller state helpers, `MainViewModel`, media models, artwork/canvas code, queue actions, lyrics models, lyrics repositories/providers, lyric settings, and tests.
- All existing lyrics formats and providers: line timestamps, word/syllable timestamps, plain/unsynchronized text, translations, romanization, offsets, cached lyrics, and manual lyric selection if supported.
- How player position is currently observed and how speed, seek, buffering, pause, track changes, crossfade, and Automix affect it.
- How animated canvas/video is loaded, decoded, muted, looped, paused, and released.
- Existing artwork color extraction, Coil image loading, Haze/blur, gradients, and low-RAM handling.
- Existing Media3 versions and APIs. Do not paste APIs from a different Media3 release.
- Existing Compose theme/dependencies. The TV composition should use TV Material where appropriate without mixing incompatible mobile and TV Material components inside one composition.

Search the full relevant call chain instead of guessing from one composable. The existing `NowPlayingScreen.kt` is large; avoid rewriting it merely to share a few values. Extract small platform-neutral state/actions where necessary.

Run a baseline build/test before editing if the environment permits. If network, SDK, signing, or emulator access blocks a command, record the exact external blocker rather than calling the code broken.

## Target architecture

Prefer this structure, adapted to current repository conventions:

```text
ui/tv/player/
  TvNowPlayingScreen.kt
  TvPlayerLayout.kt
  TvPlayerControls.kt
  TvPlayerProgress.kt
  TvPlayerBackground.kt
  TvLyricsOverlay.kt
  TvLyricsList.kt
  TvPlayerFocus.kt
  TvPlayerSemantics.kt

player/shared/
  NowPlayingUiState.kt
  NowPlayingActions.kt
  LyricsUiState.kt
  LyricsSynchronizer.kt
```

Do not force these exact filenames if equivalent layers already exist. The important separation is:

- **Existing Media3 player/service:** authoritative playback state.
- **Shared presentation state:** immutable metadata, playback state, progress, capabilities, lyrics state, and user settings.
- **TV presentation:** TV layout, focus, key handling, animations, lyrics rendering, and remote controls.
- **Phone presentation:** unchanged consumer of its existing state.

There must never be a second audio ExoPlayer created for the TV UI. The TV screen connects to the existing MediaSession/MediaController. A separate muted visual canvas player is allowed only if the current architecture requires it and it is lifecycle-safe, limited to one video decoder, and cannot affect music playback.

## Exact visual design

### 1. Full-screen background

- Fill the entire TV viewport edge-to-edge with the current track’s animated canvas when available.
- Crop using a center-aware `ContentScale.Crop`/equivalent so no letterboxing appears. Preserve the source aspect ratio; never stretch faces or artwork.
- If no canvas is available, show high-resolution album/artist artwork as a full-bleed background with a subtle, very slow scale/pan only when motion is allowed.
- If artwork is missing, use a premium BitChord gradient generated from theme colors—not a blank black screen.
- Overlay multiple restrained scrims:
  - a light top scrim for the source label;
  - a strong bottom gradient so metadata, timeline, and controls always remain legible;
  - an optional horizontal scrim where bright artwork conflicts with text;
  - a slight global darkening layer, dynamically strengthened after checking artwork luminance.
- Keep artwork recognizable. Do not cover it with an opaque black panel.
- Blur should be subtle and capability-gated. The reference is dark and cinematic, not heavily frosted.
- Background transitions between tracks should crossfade smoothly without showing the previous title on the new artwork or causing a white/black flash.
- Preload only the next safe asset when existing architecture supports it. Cancel stale loads on rapid track changes.
- Canvas audio must always be muted. Canvas playback must pause/release when the activity is stopped, lyrics mode chooses a static fallback, the system requests reduced motion, a low-RAM policy disables it, or decoding fails.

### 2. Safe area and responsive geometry

Design at a 960 × 540 dp TV baseline and verify physical output at 1280 × 720, 1920 × 1080, and 3840 × 2160.

- Keep interactive content approximately 48 dp from left/right edges and 24–30 dp from top/bottom edges.
- Respect display cutouts and system insets without creating phone-like padding.
- Metadata and progress occupy the lower quarter to third of the screen, matching the reference.
- Do not center the entire player vertically.
- Maintain the composition on wider screens using bounded widths rather than stretching control spacing indefinitely.
- Support long titles, multiple artists, non-Latin text, RTL, large font scale, and missing album/artist values without overlap.

### 3. Upper-left context label

Place a small, low-emphasis source block in the upper-left:

- BitChord icon or relevant collection icon.
- Uppercase eyebrow such as `PLAYING FROM PLAYLIST`, `PLAYING FROM ALBUM`, `PLAYING FROM ARTIST`, `PLAYING FROM QUEUE`, or the real source type.
- Source name below it.
- Derive text from real playback context. Do not hard-code fake playlist/artist names.
- If the source is unknown, use a truthful compact label or omit the block gracefully.
- This block is informational, not focusable, unless product behavior provides a real “open source” action.

### 4. Metadata row

Match the reference’s bottom-left hierarchy:

- A small square album thumbnail around 56–72 dp, positioned at the start of the progress region.
- Large bold song title aligned beside the artwork.
- Smaller artist plus album/source metadata below the title.
- Explicit-content indicator if already available.
- Lossless/codec badge only if it is accurate and does not clutter the main line.
- Marquee only after a delay and only while the metadata region is focused/active; otherwise use a clean ellipsis. Do not create constant distracting motion.
- Use high contrast and subtle shadow only when needed against artwork.

### 5. Progress timeline

Create a thin horizontal timeline beginning beside/after the thumbnail and extending nearly to the right safe margin, like the reference.

- In its resting state, use a thin low-opacity track and bright played portion.
- Current position sits below the left/start side; total duration below the right/end side.
- Format duration correctly for tracks longer than one hour and handle unknown/live duration without invalid values.
- When focused, the timeline becomes slightly thicker, exposes a clear focus indicator and seek thumb, and displays the proposed seek time.
- Left/Right seeks in consistent increments, initially 10 seconds, clamped to `[0, duration]`.
- Holding Left/Right accelerates carefully without firing a click or jumping outside the track.
- Center confirms/cancels a preview seek only if using preview mode. If seeking happens immediately, make that behavior consistent and accessible.
- On seek, synchronized lyrics must jump immediately to the correct line/word.
- Buffering progress may be shown subtly if reliable; do not confuse buffered and played progress.
- Never run a busy loop just to animate the line.

### 6. Bottom control row

Use the visual order from the reference, adapted to BitChord features:

1. Favorite/Like.
2. Shuffle.
3. Previous.
4. Large circular Play/Pause as the visual anchor.
5. Next.
6. Queue or another truthful secondary playback action.
7. A right-aligned pill button labeled **Show lyrics** or **Hide lyrics**.

Requirements:

- The primary play/pause control is a filled light circle with a dark icon, visibly larger than surrounding icons.
- Secondary controls are clean icon buttons without heavy containers when unfocused.
- Every control has clear default, focused, pressed, selected, disabled, and unavailable states.
- TV focus must be unmistakable. Use a tasteful combination of scale, outline/glow, icon/background inversion, and increased contrast. Do not rely on color alone.
- Focus animation should be fast and stable, roughly 100–180 ms, with no bouncy movement that shifts neighboring controls.
- Selected shuffle/favorite/repeat states remain visible even when not focused.
- Disabled Previous/Next controls remain readable but cannot receive focus if no action exists.
- Use actual player capabilities to decide which actions are enabled.
- Do not add an in-app master-volume control; the TV remote controls system volume.
- Queue may open the existing TV queue overlay if one exists. Do not redesign the whole queue in this task.
- Secondary actions such as repeat, speed, sleep timer, audio quality, skip silence, Automix, download, and stats may live in a focused `More` overlay if already available. Keep the main row as clean as the reference.

### 7. Controls visibility

- Controls are visible on entry, track change, pause, buffering/error, focus movement, or any remote interaction.
- During uninterrupted playback, they may fade after a sensible idle period to reveal the canvas.
- Never hide controls while any control, seek bar, dialog, or lyrics action has focus.
- Any D-pad/media interaction reveals them immediately without also triggering an unintended action on the first wake-up key. Media play/pause keys should still perform their media action.
- Keep a subtle minimal now-playing state if controls are hidden.
- Respect reduced motion and accessibility services; avoid auto-hiding if it makes operation unreliable.

## D-pad, focus, Back, and media keys

The screen must work entirely with Up, Down, Left, Right, Center/Enter, and Back.

Define and test an explicit focus map:

- Initial focus when opening the player: Play/Pause, unless returning from lyrics/queue in which case restore the invoking control.
- Left/Right across the control row follows visual order without skipping or wrapping unexpectedly.
- Up from the main control row reaches the progress timeline.
- Down from the timeline returns to the nearest logical transport control.
- The Show lyrics pill is reachable from Next/Queue and is never stranded on the far right.
- Focus cannot move behind a lyrics panel, dialog, or secondary-actions overlay.
- When an overlay closes, focus returns to the exact invoking control.
- Track changes, favorite-state changes, buffering, and recomposition must not destroy focus.
- Use stable keys and durable focus requesters. Use explicit `focusProperties` only where spatial defaults fail.

Input behavior:

- Center activates the focused control exactly once.
- Media Play, Pause, Play/Pause, Next, Previous, Fast-forward, Rewind, and Stop operate through Media3 regardless of UI focus when supported.
- Do not intercept system Home or volume keys.
- Back closes secondary menus first, then closes lyrics mode, then leaves the player through normal app navigation.
- Never display an exit confirmation for Back.
- Optional mouse/trackpad hover and click may work, but must not change remote behavior.
- Add meaningful accessibility semantics for roles, labels, checked/selected states, playback state, range values, and custom seek actions.

## Lyrics must genuinely work

Do not treat lyrics as decorative sample text. Trace and repair the complete production flow from current media item to provider response to parsed timeline to on-screen synchronization.

### Lyrics state model

Use an explicit sealed/state model equivalent to:

- `Idle` — no active media yet.
- `Loading(trackId)`.
- `SyncedLines` — timestamped lines.
- `WordSynced` — timestamped words/syllables grouped into lines.
- `Unsynced` — plain lyrics with no timestamps.
- `NotFound` — providers completed with no valid lyrics.
- `Error` — recoverable provider/network/parser failure with retry.

Never show the previous track’s lyrics while the new track is loading. Key requests by a stable media identity plus duration/version where needed. Cancel stale jobs on track change.

### Fetching and provider behavior

- Reuse all real existing lyric providers and their established priority/fallback behavior.
- Do not make a network request on every recomposition, player tick, or opening/closing animation.
- Use one in-flight request per media identity, structured concurrency, cancellation, bounded timeout, and a sensible retry policy.
- Read cached valid lyrics before network fetch where current architecture supports it.
- Cache successful parsed results and appropriate short-lived negative results without making “not found” permanent.
- Normalize title/artist/album/duration carefully for provider matching. Preserve featured artists and versions while trying safe fallback queries if the current implementation supports them.
- Reject obviously wrong lyric matches using duration/artist/title evidence instead of showing unrelated text.
- Preserve provider attribution when required by API/license terms.
- Never log full authenticated requests, cookies, tokens, or private source URLs.
- Surface a concise Retry action on network/provider error.
- `NotFound` must show a polished message such as “Lyrics aren’t available for this track,” not an endless spinner.

### Parsing and timeline correctness

- Preserve millisecond precision from the source.
- Sort valid timestamps stably, handle duplicate timestamps, empty lines, instrumental gaps, metadata tags, malformed entries, negative offsets, and missing final end-times.
- A line/word interval starts at its timestamp and normally ends at the next item’s timestamp; apply a sensible bounded fallback for the final element.
- Honor the existing user lyric offset and ensure positive/negative direction is correct.
- Never mutate cached source timestamps when applying a temporary offset.
- Word/syllable entries must remain associated with their correct line after normalization.
- Do not fabricate word timestamps from line timestamps and label them word-synced. If only line timing exists, render line sync honestly.
- Unsynchronized lyrics remain fully readable and manually scrollable.

### Synchronization engine

Drive lyrics from the authoritative Media3 player timeline:

- Update immediately after seek, media transition, discontinuity, speed change, buffering completion, play/pause, controller reconnect, and crossfade/Automix handoff.
- While paused, the highlighted word must stop advancing.
- While buffering, do not continue drifting from wall-clock time.
- Playback speed other than 1× must remain synchronized.
- Use binary search or an indexed cursor to locate the current line/word; never scan the entire lyric list on every tick.
- Use a frame-aware or bounded ticker only while lyrics are visible and playback is advancing. Avoid an unbounded `while(true)` polling loop and avoid recomposing the entire screen every 16 ms.
- Keep frequently changing progress in the smallest possible composable/state scope.
- Word-fill animation should interpolate only within the active word’s interval. Clamp fraction to `0f..1f` and handle zero/invalid durations safely.
- When the next track begins during crossfade/Automix, switch lyrics according to the authoritative current media item and position, not merely audio amplitude.
- Test timestamp boundaries exactly: just before, at, and just after each line/word start.

### Lyrics presentation mode

Clicking **Show lyrics** must transition smoothly into a TV-readable lyrics layout while preserving the cinematic background.

Preferred layout:

- Keep a darker, softened version of the active canvas/artwork as the full-screen background.
- Use the left 35–42% for compact artwork, title/artist, progress, and essential play/pause/previous/next controls.
- Use the right 58–65% for lyrics.
- Current line is large, bright, and high contrast.
- Previous and upcoming lines remain visible at reduced emphasis.
- For word/syllable sync, progressively fill/highlight the active word while the rest of the current line remains readable.
- Keep 3–7 nearby lines depending on screen size/font scale. Do not render a tiny wall of lyrics.
- Avoid a solid phone-shaped card. Use open TV spacing with a subtle gradient/scrim behind text.
- **Hide lyrics** remains clearly reachable.

If repository styling makes a full-width lyric mode stronger, it is acceptable only if metadata and basic controls remain quickly accessible and the result keeps the reference’s cinematic character.

### Auto-follow and manual browsing

- Lyrics auto-follow the current line by default using smooth, restrained scrolling.
- Do not request focus on each lyric line as playback advances.
- Up/Down while the lyrics region is intentionally focused enters manual browse mode.
- Manual mode pauses auto-follow and displays a clear **Return to current line** action.
- After a sensible idle period, auto-follow may resume only if this behavior is predictable; otherwise require the explicit action.
- Seeking or changing track exits stale manual state and moves to the correct lyric position.
- Unsynced lyrics use manual scroll without pretending to follow playback.
- Preserve the user’s chosen lyric alignment if BitChord supports Left/Center/Right positioning, adapted sensibly to the TV lyric column.
- If translations or romanization exist, provide a readable secondary line and a TV-accessible toggle without crowding every line.

### Lyrics focus and accessibility

- The scrolling lyric text itself should generally be one semantic region, not hundreds of D-pad focus stops.
- Expose the current line, lyric availability, manual/auto-follow state, and provider attribution concisely.
- Do not announce every word or syllable through TalkBack. Announce track changes and optionally current lines at a low, useful cadence only if accessibility testing confirms it is not disruptive.
- Do not rely only on red/brand color to identify the active word; use luminance/weight/opacity too.
- Support large font scale without clipping the Hide lyrics or playback controls.
- Ensure a minimum readable contrast over every artwork color through adaptive scrims.

## Loading, error, and edge states

Implement polished versions of all of these:

- Player/controller connecting.
- No current media.
- Metadata partially missing.
- Artwork loading/missing/error.
- Canvas loading/unavailable/decoder failure.
- Unknown duration or live-like media.
- Paused, playing, buffering, ended, and playback error.
- Previous/Next unavailable.
- Lyrics loading, synced, word-synced, unsynced, not found, offline error, provider error, parser error, and Retry.
- Rapid skipping across many tracks.
- Playback continuing while the TV activity is recreated.

Never leave a blank screen, permanent spinner, invisible controls, stale lyrics, impossible focus target, or crash. Playback errors should show a concise app-owned message and actionable Retry/Next where supported while preserving diagnostic details in existing logs without secrets.

## Performance and lifecycle

TV hardware may have weak GPUs and about 1 GB RAM.

- Detect `ActivityManager.isLowRamDevice` and real decoder capability.
- Request artwork near its displayed pixel size; do not decode multiple full-resolution copies.
- Keep only current/necessary adjacent visual assets in memory.
- Allow at most one active canvas/video decoder.
- Release canvas, focus overlays, and high-cost visual effects when not visible.
- Do not release the audio service/controller merely because the TV activity is backgrounded; reconnect lifecycle-safely.
- Avoid huge blurred bitmaps, per-frame allocations, shader recompilation, and whole-screen recomposition from player position.
- Use stable immutable UI state, `derivedStateOf`/equivalent carefully, and lifecycle-aware Flow collection.
- Debounce background color/artwork changes during rapid Next presses.
- Respect system animation scale and reduced-motion behavior.
- Do not use `FLAG_KEEP_SCREEN_ON` for ordinary music playback. Allow Ambient Mode/screensaver while audio continues.
- If controls are hidden and the screen remains on, prevent static high-contrast UI burn-in through normal system behavior rather than constant artificial movement.

Measure—not guess—recomposition, dropped frames, memory, image allocations, and decoder count when tools permit.

## Implementation quality rules

- Use BitChord’s existing strings/resources and add localized TV strings instead of hard-coded user-facing text.
- Keep composables reasonably sized and previewable/testable with immutable sample state.
- Do not place repositories, network calls, or MediaController creation directly inside leaf UI components.
- Hoist actions such as play/pause, previous, next, seek, favorite, shuffle, queue, show/hide lyrics, retry lyrics, and manual-scroll state.
- Use actual player command availability and current queue state.
- Do not add placeholder TODOs, fake tracks, disabled test annotations, or commented-out old implementations.
- Do not introduce a second source of truth for favorite/shuffle/play state.
- Keep phone behavior and existing playback algorithms intact.
- Preserve GPLv3 and third-party attribution.
- Do not make unrelated dependency upgrades or mass-format unrelated files.

## Required tests

Add focused automated tests using deterministic fake player and lyric data.

### Player logic tests

- Play/pause action and icon/state.
- Previous/Next availability.
- Shuffle/favorite selected state.
- Time formatting: zero, unknown, normal, and over one hour.
- Seek clamping, increments, repeated-key acceleration, and position update.
- Track transition clears stale artwork/lyrics correctly.
- Controller disconnect/reconnect preserves a usable state.

### Lyrics unit tests

- Parsing valid line-synced lyrics.
- Parsing valid word/syllable-synced lyrics.
- Duplicate/out-of-order/malformed/negative timestamps.
- Instrumental gaps and final-line duration.
- Positive and negative user offsets.
- Active line/word immediately before, at, and after timestamp boundaries.
- Pause, resume, seek forward/back, speed 0.5×/1×/2×, buffering, and media transition.
- Provider cancellation on rapid track changes.
- Cache hit, not found, error, retry, wrong-duration match rejection, and stale-result rejection.
- Unsynced lyrics behavior.

### TV Compose/focus tests

- Initial focus is Play/Pause.
- Complete Left/Right control traversal.
- Up to progress and Down back to controls.
- Progress Left/Right seek does not leak focus.
- Show lyrics opens lyrics mode and moves focus correctly.
- Hide lyrics and Back restore focus to Show lyrics.
- Manual lyric scroll, Return to current line, track change, and seek.
- Focus survives buffering, favorite/shuffle changes, metadata load, disabled Previous/Next, and rapid track transitions.
- Controls do not auto-hide while focused.
- First interaction after hidden controls does not accidentally activate a control.
- Media keys reach player actions.
- Accessibility labels, roles, states, and progress semantics exist.

### Visual/manual verification

Render or run the final player at:

- 1280 × 720.
- 1920 × 1080.
- 3840 × 2160.
- Normal and large font scale.
- LTR and RTL.
- Very bright, very dark, highly saturated, and missing artwork.
- Short and extremely long title/artist text.
- Canvas, static-art fallback, low-RAM fallback, and reduced motion.
- Lyrics hidden, line-synced, word-synced, unsynced, loading, not found, and error.

Compare against the supplied reference for composition and visual hierarchy:

- cinematic full-screen media;
- uncluttered bottom control region;
- thin timeline;
- strong central play/pause anchor;
- clear Show/Hide lyrics action;
- no phone UI residue.

Do not use a screenshot comparison as proof of interaction correctness. Test on a TV emulator with D-pad key events. If physical Android TV/Google TV hardware is available, test it and record the device. Never claim physical testing if only an emulator was used.

Run the relevant repository tasks, adapting names to actual flavors:

```bash
./gradlew test
./gradlew assembleDevDebug
./gradlew lintDevDebug
./gradlew connectedDevDebugAndroidTest
```

Also run the narrow player/lyrics test tasks directly during iteration. Repair failures caused by this implementation. If environment access blocks a command, report the exact command and blocker.

## Definition of done

The feature is complete only when all applicable statements are true:

- Opening Now Playing on TV produces the reference-inspired cinematic layout.
- It uses BitChord branding, not Spotify branding.
- Full-screen canvas/artwork fills the screen without stretching or flashes.
- Metadata, progress, time, and controls remain readable over every tested artwork.
- All player actions work through the existing authoritative Media3 session.
- The entire screen is operable with only a five-way D-pad and Back.
- Focus is always visible, predictable, restored, and never trapped or lost.
- Controls hide/reappear safely without swallowing or double-triggering input.
- Lyrics fetch real data through existing providers, cache correctly, and never remain stale after track changes.
- Line timing and word/syllable timing stay synchronized through pause, buffering, seek, speed changes, and track transitions.
- Synced, word-synced, unsynced, not-found, loading, and error states all render correctly.
- Manual lyric browsing and Return to current line work by remote.
- Lyrics are readable, accessible, and performant at 720p, 1080p, and 4K.
- Phone Now Playing behavior remains unchanged.
- No duplicate audio player, secret leakage, placeholder code, unrelated rewrite, or false test claim exists.
- Relevant builds, unit tests, lint, and available instrumentation tests pass.

## Final response

After implementation, give a concise handoff with:

1. What changed in the TV player and lyrics.
2. Main files created/modified.
3. How the finished UI matches the reference.
4. Tests and build commands actually run, with exact results.
5. Emulator/physical device, resolution, API, and input methods actually tested.
6. Any remaining external blockers or untested hardware cases.

Do not respond with only a plan. Start by inspecting the current player, lyrics pipeline, and working tree, then implement and validate this goal end-to-end.
