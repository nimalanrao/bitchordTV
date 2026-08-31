/goal

# Convert BitChord into a production-grade Android TV / Google TV music app while preserving the phone app

You are the principal Android platform engineer, TV UX lead, Media3 specialist, accessibility engineer, native-build engineer, security reviewer, and release engineer for this task. Work directly in the currently open BitChord repository and carry the implementation through to verified code. Do not stop after giving advice, writing a plan, or producing mockups. Inspect the repository, make the changes, run every test that the environment permits, repair failures caused by your work, and leave the branch in a clean, reviewable state.

Use Gemini 3.7 Flash with High thinking if the Antigravity UI exposes a thinking control. Be direct and evidence-driven. Do not narrate hidden chain-of-thought. Keep operational plans short, record durable findings in project documentation, and spend the majority of the run inspecting, editing, testing, and validating the actual code.

## Mission

Turn BitChord into one high-quality universal Android application that:

1. Preserves the existing phone/tablet app and its current behavior.
2. Adds a dedicated, remote-first Android TV / Google TV experience.
3. Runs on supported Fire TV devices wherever standard Android APIs and BitChord's dependencies permit, without assuming Google Play Services.
4. Meets every applicable current Android TV Core quality requirement and targets the Enhanced tier where practical.
5. Remains usable with only a five-way D-pad remote, while also supporting media keys, gamepads, keyboards, accessibility services, and optional pointer input.
6. Preserves BitChord's Media3 playback service, source resolution, lossless playback, downloads, local music, account features, lyrics, queue, crossfade, Automix, scrobbling, and Discord integration unless a specific feature is impossible on a TV device; any unavoidable exception must be feature-gated and documented rather than silently broken.
7. Produces a signed APK through the existing release path and a Play-ready Android App Bundle, with current ABI and 16 KB page-size compliance proven by tooling.
8. Introduces no regressions, secret leakage, insecure authentication shortcuts, licensing violations, or false claims about tests or device coverage.

This is an implementation goal, not a request for a speculative redesign.

## Scope truth: define “TV compatible” correctly

Do not claim that an Android APK can run on every television platform. The deliverable must explicitly distinguish:

- **In scope:** Android TV OS, Google TV devices, Android TV emulators, and Android-based set-top boxes that meet the app's API and hardware requirements.
- **Compatibility target, with physical verification required before claiming certification:** Fire TV devices on a supported Fire OS/API level. The current app has `minSdk 26`, so do not promise Fire OS 5 or Fire OS 6 support; Fire OS 7 and newer are the reasonable starting target unless repository/dependency evidence supports a broader range.
- **Out of scope for this Android codebase:** Samsung Tizen, LG webOS, Roku OS, Apple tvOS, VIDAA, and other non-Android TV operating systems. Supporting those requires separate applications or a separately approved cross-platform strategy.

Create `docs/TV_COMPATIBILITY.md` with this matrix, tested devices/emulators, untested targets, API/ABI limitations, and exact evidence. Never convert “builds” into “works on hardware” without a real run.

## Repository snapshot and facts to verify before editing

The repository is `https://github.com/kushagrasinghx/BitChord`. The prompt was prepared against commit `5d137d1aa9ea3ffdef0dbffbc861c595d21fca47` (`5d137d1`, 2026-08-30), but the working copy may have advanced. Begin by recording the actual branch, HEAD, working-tree status, and relevant build versions. Preserve all pre-existing user changes. Never reset, overwrite, or reformat unrelated work.

The reference snapshot had these properties. Treat them as audit leads, not unquestionable truth:

- One Android `app` module with `dev` and `prod` flavors.
- `compileSdk 36`, `targetSdk 36`, `minSdk 26`.
- AGP 8.10.1, Gradle 8.11.1, Kotlin 2.3.20, Compose BOM 2024.12.01, and Media3 1.11.0.
- Kotlin/Compose phone UI plus a C++ analyzer and third-party native libraries, including ONNX Runtime Android and QuickJS.
- `MainActivity` declared portrait-only and currently used as the ordinary launcher.
- No TV `LEANBACK_LAUNCHER`, TV banner, or complete TV feature declarations in the manifest.
- Phone-centric navigation and interaction patterns: bottom navigation, mini-player, modal bottom sheets, pull-to-refresh, swipe actions, long-press actions, touch sliders, and touch-first search/sign-in flows.
- Very large UI files, including `MainActivity.kt` and `NowPlayingScreen.kt`; avoid an indiscriminate rewrite.
- Reusable non-UI layers: `MainViewModel`, repositories, models, Media3 controller/state helpers, source modules, downloads, lyrics, and `PlaybackService`.
- `PlaybackService` already uses `MediaSessionService`, audio-focus-aware ExoPlayer configuration, a `MediaSession`, and a session activity that currently resolves to `MainActivity`.
- Native ABI filters limited to `arm64-v8a` and `x86_64` in the reference snapshot. That is not sufficient for current TV publishing expectations requiring both 32-bit and 64-bit support.
- Existing GitHub release automation builds a signed production APK but not necessarily an AAB or TV validation artifacts.
- Existing GPL-3.0 licensing and legal notices must remain intact.

Search the whole repository before choosing architecture. Inspect at least:

- root and module Gradle scripts, version catalogs if any, Gradle properties, wrapper, signing setup, CI workflows, ProGuard/R8 rules, and flavor/source-set behavior;
- every manifest and manifest placeholder;
- `MainActivity`, app/root navigation, destination definitions, theme setup, and all major phone screens;
- `PlaybackService`, MediaSession callbacks, custom commands, notification/media-button handling, audio focus, audio attributes, becoming-noisy handling, PendingIntents, process lifecycle, and foreground-service shutdown;
- `MainViewModel`, player/controller helpers, repositories, database/storage, downloads, local media permissions, auth stores, source modules, network-quality selection, artwork/video-canvas loading, lyrics, queue, and settings;
- every JNI/CMake file, every packaged `.so`, every native AAR, and ABI/page-alignment metadata;
- image resources, icons, `Banner.png`, `Logo.png`, strings, translations, RTL behavior, and accessibility semantics;
- current tests and all buildable variants.

Use `rg`/repository search rather than guessing. If a fact in this prompt differs from current code, trust current code, note the difference in the implementation report, and adapt without weakening the acceptance criteria.

## Non-negotiable engineering constraints

1. **Preserve phone behavior.** Keep `MainActivity` and the existing handheld UI working. Do not merely make the portrait UI landscape and call it a TV UI.
2. **Use a dedicated TV entry point and composition.** The preferred architecture is the same package/app with a `TvActivity` and a TV-specific Compose navigation root that shares domain and playback layers. Choose a separate module only if a written dependency/build analysis proves it materially safer.
3. **Remote-first operation.** Every task must be possible without touch, swipe, long press, a mouse, or a phone companion.
4. **No Material split-brain in one composition.** A TV composition should use `androidx.tv.material3.MaterialTheme` and compatible TV components. Do not nest or casually mix mobile Material 3 and TV Material themes/components in the same composition. Shared business logic and carefully neutral primitives are fine.
5. **Use current Compose Foundation lazy layouts.** Do not introduce deprecated `TvLazyRow`, `TvLazyColumn`, or `TvLazyGrid`; use `LazyRow`, `LazyColumn`, and grids from Compose Foundation, with TV-specific focus/bring-into-view behavior.
6. **Do not hijack D-pad keys globally.** Directional keys must move focus predictably except inside a clearly focused adjustable control or a documented Now Playing transport mode. Center/Enter activates the focused item. Media play/pause keys control playback regardless of focus.
7. **Security over convenience.** Never log cookies, tokens, credentials, source secrets, Discord tokens, Last.fm credentials, or signed URLs. Never implement a home-grown QR code that transports Google cookies or credentials. Do not weaken TLS or WebView security. Credentials, if entered, go only to the intended provider surface. Preserve encrypted storage and remove/fix any unsafe fallback only after migration analysis.
8. **No invented API behavior.** Verify Media3 and Compose APIs against the versions actually selected. Use official documentation and source/release notes. Do not paste obsolete snippets.
9. **No fake completeness.** Never mark a physical device, sign-in provider, voice path, ABI, native library, 16 KB page-size test, or store checklist item as passing unless the evidence exists.
10. **No broad cleanup disguised as TV work.** Refactor only where necessary to share logic, create TV UI, improve testability, or satisfy a stated quality gate.
11. **No placeholders.** Do not leave mock implementations, empty click handlers, `TODO()` calls, disabled tests, hard-coded sample media, or comments claiming future work in the finished path.
12. **Respect GPLv3 and third-party notices.** Preserve copyright headers, source availability, license screens, and distribution obligations. Document new third-party dependencies and their licenses.
13. **Do not change application IDs or destroy update compatibility.** Retain existing dev/prod application ID behavior and signing flow.
14. **Do not silently lower media quality.** Any TV-specific decoder, memory, blur, artwork, animated-canvas, or Automix fallback must be capability-driven, visible in diagnostics where useful, and documented.

## Required execution workflow

Follow these phases. Continue automatically between phases; ask the user only when blocked by missing authority, an unavailable secret/device, an ambiguous destructive choice, or a product decision that genuinely cannot be inferred.

### Phase 0 — Protect and baseline

1. Record `git status`, branch, HEAD, remotes, Java/Gradle/SDK/NDK versions, and available emulator/device targets.
2. Identify existing uncommitted changes and avoid touching unrelated hunks.
3. Run the smallest meaningful baseline commands before editing, such as debug unit tests, dev debug assembly, lint, and any existing checks. If dependency/network/toolchain access blocks them, record the exact command and failure; do not call it a repository failure.
4. Capture the current merged manifest for relevant variants if the project builds.
5. Create a concise audit table mapping current behavior to each applicable TV Core requirement.

### Phase 1 — Architecture and dependency decision

Before making a broad edit, write a short architecture note in `docs/TV_IMPLEMENTATION_REPORT.md` answering:

- Why a same-package universal application with `MainActivity` plus `TvActivity` is or is not appropriate.
- Which code is platform-neutral and will be shared.
- Which UI/state must be extracted from enormous phone composables without destabilizing them.
- Which current dependencies are usable in a TV composition.
- Which official stable versions of Compose for TV, Media3, NDK, and build tooling are selected and why they are compatible.
- How navigation/deep links/MediaSession PendingIntents select the correct activity.
- How low-RAM, no-touch, no-microphone, Ethernet, no-vibrator, and no-Google-Play-Services devices behave.
- How native 32/64-bit and 16 KB requirements will be met.

Prefer adding a thin TV presentation layer over duplicating repositories, player services, databases, downloads, or source logic.

### Phase 2 — Build and manifest foundation

Implement a universal app manifest that keeps the phone launcher and adds the TV launcher correctly. At minimum, after verifying current requirements:

- Declare `android.software.leanback` with `android:required="false"` so the same artifact can serve phones and TVs.
- Declare `android.hardware.touchscreen` with `android:required="false"`.
- Declare `android.hardware.faketouch` with `android:required="false"` if dependency/manifest merging would otherwise require it.
- Audit all other hardware features introduced transitively. Camera, microphone, telephony, location, GPS, accelerometer, portrait orientation, and similar features must not become required merely because an API is referenced.
- Add a proper application TV banner resource and wire `android:banner` as required by current launcher/store behavior.
- Keep the ordinary phone `MAIN` + `LAUNCHER` route.
- Add an exported `TvActivity` with `MAIN` + `LEANBACK_LAUNCHER`, landscape orientation, an appropriate TV theme, and launch mode/task behavior that prevents duplicate playback tasks.
- Do not place `LEANBACK_LAUNCHER` on the portrait phone activity.
- Ensure TV launch does not flash or instantiate the phone UI.
- Add intent/deep-link routing only where needed and verify it does not create two visible launcher entries on one form factor.
- Keep `minSdk <= 31` as required for broad TV availability; do not raise the current `minSdk 26` without a hard technical reason.

Implement a small, testable form-factor classifier using `UiModeManager` and/or `PackageManager.FEATURE_LEANBACK`, with sensible behavior for unusual Android TV boxes. Do not scatter raw device checks across composables.

Update the MediaSession session-activity PendingIntent so notification/Now Playing/Assistant re-entry opens `TvActivity` on a television and `MainActivity` elsewhere. Use stable request codes, correct immutable/update flags, and verified task-stack semantics. Preserve incoming media/deep-link intent data where applicable.

Add or update dependencies with minimal version churn. Select the current compatible stable `androidx.tv:tv-material` artifact and use its `androidx.tv.material3` APIs in the TV composition. Do not perform a sweeping Compose/AGP/Kotlin upgrade unless compatibility requires it and the entire project passes afterward.

### Phase 3 — Brand and launcher assets

Create production-quality TV assets from the repository's existing BitChord branding; do not invent unrelated branding.

- Add a 320 × 180 px xhdpi TV banner containing both the BitChord icon/branding and readable app name, with safe margins and no transparency/cropping defect on light or dark launchers.
- Retain or improve adaptive and legacy app icons as necessary for TV launchers.
- Use high-resolution artwork appropriate for 1080p; provide higher-density/4K-ready assets where the quality tier calls for them.
- Never stretch the existing banner or logo. Preserve aspect ratio and create deterministic source assets if regeneration is needed.
- Verify actual packaged resource dimensions and manifest references, not just source-image filenames.
- Add content descriptions only where an in-app brand image conveys information; decorative background images should not create screen-reader noise.

### Phase 4 — TV design system and shell

Build a dedicated TV design system under a clear package such as `ui/tv/`. Names may differ if repository conventions demand it. Include:

- TV theme, color, type, dimensions, shapes, motion, and focus styles based on the current BitChord artwork-driven aesthetic.
- TV-safe alternatives for Haze/frosted glass. Blur may be used only when GPU/memory behavior is acceptable; offer an opaque/translucent fallback for low-RAM devices, reduce-motion settings, and devices where rendering is unstable.
- Reusable focusable cards, buttons, icon buttons, chips, list rows, settings rows, dialogs, menus, progress indicators, empty/error/loading states, metadata labels, and an adjustable slider/seek component.
- Strong states for default, focused, pressed, selected, disabled, loading, playing, and error. Focus must be recognizable without relying only on color. Use restrained scale, outline/glow, elevation, and contrast consistent with TV guidance.
- A layout baseline designed at 960 × 540 dp and verified at 1280 × 720, 1920 × 1080, and 3840 × 2160 output. Keep critical content in a TV-safe area approximately 5% from physical edges; use around 48 dp horizontal and 24–27 dp vertical as a starting point, then verify visually.
- Readable ten-foot typography and truncation/marquee rules. Do not shrink essential text to fit.
- Responsive handling for 16:9, wider displays, display-size/font-scale changes, and overscan-prone hardware.

Use motion purposefully. Focus transitions should be quick and stable. Avoid endless large-area animation, rapid parallax, seizure risks, or motion that competes with lyrics. Respect system animator scale and implement a reduced-motion path.

Build the root TV shell with:

- A collapsed/expanded navigation rail or drawer suitable for remote use.
- Roughly 3–7 primary destinations after inventorying current features. A reasonable initial information architecture is Home, Search, Library, Downloads/Local, and Settings, with Now Playing globally reachable. Adjust names to actual product concepts.
- An always-clear current destination and stable focus return when the drawer closes.
- A compact global playback affordance that shows artwork/title/state and opens Now Playing, but never becomes an unreachable phone-style bottom sheet.
- A single top-level background treatment that can reflect artwork while remaining legible and cheap enough for TV hardware.

Do not port phone bottom navigation to TV. Do not make primary navigation depend on a hamburger icon or pointer.

### Phase 5 — Focus and input architecture

Treat focus as a first-class state machine, not a cosmetic modifier added at the end.

For every screen and overlay:

1. Define the initial focus target.
2. Make every visible actionable control reachable with Up/Down/Left/Right.
3. Make focus movement spatially predictable.
4. Ensure focus never lands on decorative or disabled elements.
5. Ensure focus never disappears after recomposition, loading, list mutation, navigation, dialog close, item removal, or player-state change.
6. Restore the last meaningful focused item and scroll position when returning to a destination.
7. Use stable item keys and durable focus identifiers; do not retain stale `FocusRequester` objects for removed rows.
8. Use `focusGroup`, `FocusRequester`, `focusRestorer` where compatible, `focusProperties`, and `BringIntoViewSpec` intentionally. Add explicit directional overrides only where spatial defaults genuinely fail.
9. Ensure the focused item is fully or meaningfully visible, including the first/last row near safe-area boundaries.
10. Prevent wraparound surprises, focus traps, cross-row diagonal jumps, and focus escaping behind dialogs.

Input requirements:

- D-pad Up/Down/Left/Right, center/Enter/NumpadEnter, Back/Escape, media play, pause, play-pause, next, previous, fast-forward, rewind, and stop must behave correctly.
- Gamepad A/B and keyboard Enter/Escape should map naturally through Android key behavior where possible.
- Pointer hover/click support is additive; it must not change remote semantics.
- Long press may be an optional accelerator but never the only route to an action.
- Handle repeated key events on adjustable controls without double-triggering click actions.
- Do not intercept Home. Respect system behavior.
- Use predictive/system Back correctly. Back first dismisses the topmost transient surface, then returns through app navigation, then exits from the root. Never show an exit confirmation and never make Back toggle between two states forever.

Write reusable Compose UI tests for the focus system and key handling. A screen that merely draws successfully is not considered remote-compatible.

### Phase 6 — Replace every touch-only interaction

Inventory the complete phone app and map each pattern to a TV equivalent. At minimum:

| Phone pattern | Required TV pattern |
|---|---|
| Bottom navigation | TV navigation rail/drawer |
| Modal bottom sheet | Centered dialog, side panel, or full-screen TV overlay with focus trap and deterministic return |
| Pull to refresh | Explicit focusable Refresh/Retry action; optional background refresh |
| Swipe to remove/reorder/play next | Visible context action/menu; optional D-pad reorder mode with instructions and cancel |
| Long press for menu | Visible More/Options button and standard click path |
| Touch slider | Focusable adjustable control; Left/Right changes value, center can enter/exit adjustment if needed, semantics expose range/value |
| Drag-only seek | Focusable seek bar with clear time labels, bounded key increments, fast repeat, and screen-reader range semantics |
| Phone mini-player sheet | Global playback card plus dedicated Now Playing destination |
| Touch-only carousel | Lazy row with predictable focus, edge peeking, and restored row/item position |
| Mobile keyboard assumptions | System TV IME, hardware keyboard, and remote-safe submit/cancel behavior |
| On-screen back arrow as sole route | System Back; optional visual close only when it helps pointer/accessibility users |

Search for `clickable`, `combinedClickable`, pointer/touch gestures, swipe, drag, nested scroll, pull refresh, bottom sheets, dropdowns, popups, sliders, and long-click handlers. Prove there is a TV route for every user-visible action.

### Phase 7 — TV Home/browse experience

Create a TV-first catalog/browse screen using a vertically scrolling set of horizontal content shelves where appropriate. It should feel like BitChord, not a generic video template.

Requirements:

- Reuse actual Home/browse data from repositories and view models.
- Show meaningful shelves such as Continue Listening, Recently Played, Recommendations, Albums, Artists, Playlists, or the repository's real concepts. Do not fabricate endpoints.
- Provide loading skeletons that do not steal focus, concise errors with Retry, and useful empty states.
- Card aspect ratios must match content type; album art remains square and is never cropped into a video poster.
- Each card exposes title, subtitle/artist, selected/playing state where relevant, and an accessible action label.
- Center click performs the obvious primary action. A visible options affordance exposes secondary actions: Play next, Add to queue, Favorite, Download, Go to album/artist, and Remove where supported.
- Rows restore their selected item after visiting a detail page. The screen restores the selected row after navigating back.
- Progressive image loading must request approximately the displayed pixel size and avoid decoding enormous originals for small cards.
- Background/artwork transitions must be debounced and cancellation-safe so rapid focus movement does not trigger network/GPU churn.
- Browsing must remain usable offline with cached/local/downloaded content and honest network errors.

Add TV detail screens for the actual entity types the app supports: playlist, album, artist, song/menu, download/local collection, and any source-specific result. Reuse domain models and actions. Avoid phone-width center columns floating in empty landscape space.

### Phase 8 — Search and text input

Create a dedicated TV search experience:

- A clearly focused search entry opens or integrates with the standard Android TV IME. Do not build a custom keyboard unless a proven platform defect requires one.
- Support D-pad-only entry, hardware keyboard input, editing, clearing, submission, cancellation, loading, empty results, recent searches if already supported, and error recovery.
- Preserve current search categories and source behavior.
- Add voice search only through standard Android intents/APIs when available. Do not declare microphone hardware required. Detect availability and hide/disable gracefully.
- If supporting global/Assistant media search intents, parse them safely, route them through existing repositories, and add tests for empty/malformed queries.
- Never auto-play an ambiguous voice result without an explicit, documented product rule.
- Return focus to the prior query/result when navigating back.
- Do not expose sensitive search history to logs.

### Phase 9 — TV Now Playing, queue, and lyrics

Build a dedicated landscape Now Playing screen rather than stretching the current phone composable. Share state/actions with the phone player.

A strong default layout is a two-pane composition:

- Left or dominant pane: square album artwork or animated canvas, playback state, and restrained artwork-driven background.
- Right/supporting pane: title, artist, album, timing, transport controls, favorite/repeat/shuffle, audio quality/stats affordance, lyrics/queue tabs or panels, and contextual actions.

Adapt if accessibility or content density proves a better arrangement. Requirements:

- Title/artist and play state are immediately legible from viewing distance.
- Primary transport controls have large hit/focus targets and obvious selected/focused states.
- Media-center behavior is context-aware: when a passive Now Playing surface/transport mode has focus, center can toggle play/pause; when a specific button is focused, center activates that button. Do not make it impossible to select other controls.
- Left/Right seeking is available through a focused seek bar or explicit transport mode, with a documented default increment such as ±10 seconds and acceleration for repeat. Ordinary horizontal focus movement must remain possible outside that mode.
- Previous/Next, play/pause, rewind/fast-forward, repeat, shuffle, favorite, speed, skip silence, sleep timer, quality/stats, lyrics, queue, source, download, and Automix controls appear when the underlying feature supports them. Group secondary actions rather than crowding the primary row.
- Do not implement an in-app master volume slider. Volume keys/system UI control TV volume. If the app has a gain control distinct from volume, label it accurately.
- Queue browsing and item actions are fully remote-operable. If queue reordering is supported, use an explicit reorder mode with Move up/down, Commit, and Cancel behavior; never require drag.
- Removing the focused queue item must move focus predictably to the next/previous item.
- Animated album canvas uses no more than one video decoder and falls back to static artwork for low-RAM, reduced-motion, unavailable codecs, backgrounded activity, errors, or Ambient Mode.
- Release visual player resources when the TV activity stops while allowing the audio service to continue.

Lyrics requirements:

- Preserve word/syllable sync if current data supports it.
- Use TV-readable line length, size, spacing, and contrast.
- Clearly distinguish current, previous, and upcoming lines without relying only on color.
- Provide auto-follow and a manual browsing mode. Manual movement temporarily suspends auto-follow, exposes a clear Return to current line action, and resumes predictably.
- Do not continuously steal focus as the lyric timestamp changes.
- Screen readers receive useful line/current-position information without an announcement on every syllable.
- Lyrics are not system captions; do not claim caption compliance merely because lyrics render.

### Phase 10 — Media3, system integration, and audio correctness

Preserve the existing `MediaSessionService` and working playback engine unless a focused refactor is required. Audit against the current selected Media3 APIs.

Required behavior:

- Notification/system controls, Bluetooth/headset buttons, TV remote media keys, Assistant/system transport commands, and app UI all control one authoritative session/player.
- The session advertises appropriate standard commands and available player commands. Do not replace standard play/pause/seek/next/previous behavior with custom commands.
- If current Media3 supports `setMediaButtonPreferences`, use it appropriately and compatibly rather than depending only on deprecated custom layouts.
- Custom commands such as Favorite or Automix remain permission-checked and do not shadow standard commands.
- Media items expose accurate media ID, title, artist, album, duration when known, artwork URI/data policy, playable/browsable state where applicable, and queue metadata.
- System Now Playing returns directly to a usable TV Now Playing screen and can pause/resume the session.
- Audio focus is requested and abandoned correctly. If ExoPlayer already uses `setAudioAttributes(..., handleAudioFocus = true)` for the owning player, avoid adding a second competing focus manager.
- Becoming-noisy, HDMI disconnect, Bluetooth route change, transient focus loss, ducking, phone/communication interruptions where applicable, and output-device change behave safely.
- Ethernet counts as an unmetered network unless Android reports it metered. Retain per-network quality logic without assuming Wi-Fi is the only TV connection.
- Gapless, crossfade, lossless source selection, fallback to YouTube Music, speed, skip silence, Automix, and downloads are regression-tested. Do not “simplify” the service into a basic player.
- The foreground service is active only while required for playback/download behavior and stops foreground/service state when playback truly ends according to platform policy.
- The activity does not set `FLAG_KEEP_SCREEN_ON` for ordinary audio playback. Allow Ambient Mode/screensaver while audio continues.
- If a `MediaLibraryService` would materially improve system browse/voice integration, first document the migration cost and compatibility. Do not rewrite a functioning `MediaSessionService` solely because a sample uses a library service.

Implement the Android TV Home-screen Now Playing integration required for audio apps: correct MediaSession state/metadata, activity PendingIntent, playback controls, and re-entry. Verify with `adb`/system UI where possible.

### Phase 11 — Accounts, WebView, providers, and permissions

Make existing sign-in/account flows TV-operable without weakening them.

- Inventory YouTube/Google cookie sign-in, Discord, Last.fm, ListenBrainz, and module-source credentials.
- Use a full-screen or appropriately sized TV sign-in surface with deterministic D-pad focus, system IME support, progress/error/cancel states, and Back behavior.
- Keep provider credentials inside the provider's intended WebView/browser surface. Do not intercept password fields or render them in app-owned logs/UI.
- Use a modern secure WebView configuration: no unnecessary file/content access, no mixed content, safe browsing where available, restricted JavaScript bridges, allowlisted navigation only where the current auth design permits, and lifecycle cleanup. Do not break legitimate provider redirects.
- If Google rejects embedded WebView authentication on a given device, report and document the actual provider limitation. Do not evade bot/security checks or invent cookie transfer.
- Preserve encrypted credential/token storage. If a plaintext fallback exists, audit why, create a safe migration if possible, and never silently delete users' sessions.
- Add logout/revoke/clear-session actions that work by remote and accurately describe what they clear.
- Fire TV must not crash because Google Play Services or a Google-specific voice/IME component is absent.
- Permissions must be requested contextually with a D-pad-selectable rationale and graceful denial state. No touch-only permission recovery.
- Local audio access must follow current Android media/storage permission APIs for each supported SDK. TV devices without local storage/music return a useful empty state.

### Phase 12 — Settings and feature parity

Create a TV-native Settings destination. Reuse existing setting keys, stores, validation, defaults, and side effects; do not fork user preferences.

- Render settings as focusable rows grouped into comprehensible sections.
- Booleans toggle with center and expose checked semantics.
- Enumerations open a focused radio dialog/list.
- Numeric ranges use a D-pad adjustable component with current value and units.
- Text/secrets use the system IME and masked display where appropriate.
- Module sources can be added, edited, tested, health-checked, reordered if supported, and removed without touch gestures.
- Audio quality, crossfade 0–12 s, Automix, playback speed 0.5×–2.0×, skip silence, animated canvas, lyrics, downloads, scrobbling, Discord, cache, diagnostics, and other real settings remain reachable when applicable.
- System equalizer integration must feature-detect an activity/provider and show an honest unavailable state rather than crash.
- Replace phone-specific labels such as “swipe” or “tap and hold” in TV string resources.
- Settings dialogs trap focus and return it to the invoking row on close.
- Destructive actions require an accessible confirmation only when data loss warrants it; ordinary navigation and app exit do not.

### Phase 13 — Performance, memory, lifecycle, and resilience

Assume many TVs have slow CPUs, weak GPUs, limited storage, and about 1 GB RAM.

Implement and verify:

- A centralized capability/performance policy using `ActivityManager.isLowRamDevice` plus actual feature/decoder availability—not brand/model allowlists.
- On low-RAM devices, target under 280 MB total application memory and preferably under 200 MB anonymous + graphics memory during normal playback/browse. Measure with official tools; do not claim targets from code inspection.
- Correctly sized Coil/image requests, memory/disk cache limits appropriate for TV, RGB configuration only where quality allows, no duplicate full-resolution bitmaps, and cancellation/debounce during rapid focus changes.
- At most one animated-canvas/video decoder. Release it when not visible, backgrounded, in Ambient Mode, or memory-constrained.
- Static-art and reduced-effects fallbacks for canvas, blur, mesh gradients, glow, and parallax.
- No large per-frame allocations in lyrics, visualizers, gradients, or focus animations.
- Stable Compose models, keys, derived state, and snapshot usage to prevent recomposition storms.
- Lifecycle-correct collection (`collectAsStateWithLifecycle` or equivalent), controller connection/release, WebView teardown, broadcast receiver registration, and native resource cleanup.
- Audio keeps playing when the TV activity is backgrounded or the screensaver activates.
- Database/network/download work stays off the main thread and survives ordinary process recreation according to existing architecture.
- Low storage, offline, DNS failure, source timeout, malformed artwork, missing codec, missing lyrics, unavailable provider, and corrupt local file paths produce recoverable states.
- State restoration after activity recreation and process death where practical: destination, selected entity, playback session reattachment, and safe focus fallback.

Add a Baseline Profile and Macrobenchmark coverage if the repository/tooling can support it without destabilizing the release. At minimum cover cold start to first meaningful TV content, opening Now Playing, and scrolling a representative shelf. Record the profile generation command and measured before/after evidence; never fabricate performance numbers.

### Phase 14 — Native ABIs and 16 KB page-size compliance

This phase is release-blocking. The reference repository's 64-bit-only assumption is not acceptable for current TV distribution expectations.

1. Inventory every native object in app dependencies and the built artifact. Include the custom analyzer, ONNX Runtime, QuickJS, and transitive AAR/JAR contents.
2. Provide at least `armeabi-v7a` and `arm64-v8a` for physical TV distribution. Retain `x86_64` where useful for the official emulator. Add legacy `x86` only if a real supported test/distribution target and every dependency justify it.
3. Do not merely add an ABI filter if a dependency lacks that ABI. Prove each packaged native feature loads and works. Update to a compatible official dependency, build from source under its license, or capability-gate the feature safely. Document any unavoidable limitation.
4. Pin/use NDK r28 or newer if compatible so project-built native libraries receive 16 KB alignment by default. If using an older NDK is unavoidable, add the official linker configuration deliberately and explain it.
5. Confirm AGP/package alignment supports 16 KB pages. Do not rely on AGP version alone; prebuilt libraries must also be aligned.
6. Inspect ELF program headers for every `.so` and verify `LOAD` alignment is at least `2**14` where required, with valid RELRO behavior.
7. Verify APK zip alignment using the current official `zipalign -v -c -P 16 4 <apk>` form.
8. Verify the AAB requests 16 KB alignment with `bundletool dump config` and `PAGE_ALIGNMENT_16K` evidence.
9. Run on an Android 15+ 16 KB emulator/device, verify `adb shell getconf PAGE_SIZE` returns `16384`, launch all native-dependent paths, play media, and exercise Automix/analyzer behavior.
10. Run both 32-bit and 64-bit TV images/devices if available. Record exact system image, API, ABI, and outcome.

If a third-party native binary blocks armv7 or 16 KB, do not hide it. Continue with all safe work, isolate the blocker, identify the precise artifact/version/symbol/alignment, propose the narrowest compliant remedy, and mark the release gate failed until proven fixed.

### Phase 15 — Accessibility and internationalization

Test with Android TV TalkBack and, when targeting Fire TV, VoiceView on real hardware if available.

- Every actionable control has a concise label, role, state, and action semantics.
- Do not focus both a parent card and all decorative/text children, causing duplicate navigation.
- Group metadata semantically so album cards are announced once, meaningfully.
- Expose selected, checked, expanded, playing, paused, disabled, progress, and range states.
- Provide content descriptions for icon-only controls; do not add redundant descriptions to visible text.
- Focus indicators meet strong contrast/readability expectations and work with color-vision deficiencies.
- Aim for at least 4.5:1 contrast for ordinary text and 3:1 for large text/non-text indicators where applicable; document intentional artwork-overlay treatment and test it against varied images.
- Support font scaling and display scaling without clipping critical actions.
- Verify RTL mirroring, focus order, sliders/seeking semantics, icons that should/should not mirror, and mixed-direction artist/title strings.
- Do not rely on transient toast messages for essential errors; show a focused, persistent recovery action.
- Honor reduced motion/system animation scale and avoid autoplaying visual effects that cannot be reduced.
- Do not flood accessibility announcements from word-synced lyrics or playback progress.
- Pointer targets and remote focus targets should be generously sized; remote users should never need pixel precision.

Add accessibility checks to UI tests where supported, but do not treat automated checks as a substitute for TalkBack/VoiceView traversal.

### Phase 16 — Test matrix and acceptance evidence

Create `docs/TV_TEST_MATRIX.md`. Separate **automated passed**, **emulator passed**, **physical device passed**, **manual pending**, **blocked**, and **not applicable**. Include date, commit, build variant, API, ABI, resolution, density, page size, RAM class, input device, and evidence location.

At minimum cover this matrix where infrastructure permits:

- Oldest supported Android TV API level or nearest available official image.
- A 32-bit ARM target/device.
- A 64-bit ARM target/device.
- Current 1080p Google TV emulator.
- 720p output.
- 4K output.
- Android 15+ / API 35 or newer 16 KB page-size image.
- Low-RAM behavior.
- Fire TV OS 7+ physical device if available.
- D-pad remote, media keys, keyboard, gamepad if available, optional pointer, TalkBack, and VoiceView if available.
- LTR, RTL, large font/display scale, dark artwork, light artwork, very long titles, missing artwork, and non-Latin metadata.
- Online Ethernet, Wi-Fi if available, metered network simulation, offline, slow/failed source, and recovery.

Automated requirements:

1. Unit tests for form-factor classification, route/activity selection, TV navigation/state reducers, settings transformations, media command availability, and relevant error/fallback policy.
2. Compose UI/instrumentation tests that issue actual D-pad key events through every TV screen. Assert initial focus, directional order, focus restoration, overlay trapping, Back sequence, list mutation, and no-focus states.
3. UI tests for Home, Search, detail pages, Library/Downloads/Local, Now Playing, queue, lyrics, Settings, account dialogs, source editor, errors, and empty/loading states using deterministic fake repositories/player state where necessary.
4. Manifest/resource tests or scripted checks for Leanback launcher, optional touchscreen, banner, landscape TV activity, both phone and TV launch paths, and no accidentally required hardware.
5. Media tests for play/pause, center behavior, next/previous, seek, media keys, notification/system controls, session re-entry, Home/background audio, becoming noisy, and service shutdown.
6. Regression tests for phone routes and existing business logic.
7. Screenshot or render-based review at 720p, 1080p, and 4K for safe areas, clipping, focus appearance, dialog placement, and readable type. Do not use screenshot snapshots as the sole focus test.
8. Native checks described in Phase 14.

Run the repository-equivalent of these commands after adapting to actual variants/tasks:

```bash
./gradlew --version
./gradlew test
./gradlew assembleDevDebug
./gradlew lintDevDebug
./gradlew assembleProdRelease
./gradlew bundleProdRelease
./gradlew connectedDevDebugAndroidTest
```

Also run dependency/license checks, baseline profile generation, macrobenchmarks, and release verification tasks that actually exist. Never blindly suppress lint, test, or R8 failures. A release-signing secret unavailable in the environment may block the signed release task; prove an unsigned/release-equivalent build and record that external blocker without changing signing security.

Manual D-pad traversal checklist for every screen:

- Launch creates exactly one obvious initial focus.
- Every control is reachable and visible.
- Directional movement matches spatial expectation.
- No focus loop, trap, escape behind overlay, disappearance, or accidental row jump.
- Center/Enter performs the advertised action once.
- Repeated direction keys do not trigger duplicate clicks.
- Back closes one layer at a time and exits from root.
- Returning restores meaningful row/item focus.
- Loading-to-content, content-to-error, list insertion/removal, and account/player state change retain a valid focus target.
- TalkBack announcements are concise and actions remain operable.

Playback checklist:

- Cold launch with no session.
- Reattach to an active background session.
- Play local, streamed fallback, lossless source, downloaded, and queued media.
- Gapless/crossfade/Automix boundaries.
- Pause/resume/seek/next/previous/repeat/shuffle/favorite.
- Speed, skip silence, sleep timer, stats, lyrics, queue actions.
- Home key then system Now Playing re-entry.
- Ambient Mode while audio continues.
- Audio focus loss/gain, noisy route, HDMI/Bluetooth change where testable.
- Process/activity recreation and recoverable network/source failures.

### Phase 17 — CI and release artifacts

Update GitHub Actions conservatively:

- Preserve existing signing setup and never print secrets.
- On pull requests, run deterministic debug/unit/lint checks that do not need signing secrets.
- On release/manual workflows, produce the existing signed production APK plus a production AAB.
- Upload clearly named APK/AAB, mapping/native-symbol files where policy permits, lint/test reports, and TV/native validation summaries.
- Add scripted merged-manifest and ABI/16 KB checks.
- Add emulator instrumentation only if the runner can execute it reliably; otherwise create a separate/nightly/manual job and do not make a permanently flaky required check.
- Cache Gradle safely; pin action versions; use least permissions; avoid untrusted PR access to signing secrets.
- Ensure dev and prod flavor task names are correct from Gradle, not assumed.

Create an exact release checklist in `docs/TV_RELEASE_CHECKLIST.md` covering:

- Android App Bundle generation.
- TV banner, icon, screenshots, feature graphic/listing assets, and TV category opt-in.
- Package/application ID and versioning.
- Core/Enhanced TV quality mapping.
- 32/64-bit and 16 KB evidence.
- Privacy policy, data safety, account deletion/log-out behavior where applicable.
- GPLv3 Corresponding Source and third-party notices.
- Content/API/legal disclaimer consistency.
- Physical-device signoff and rollback plan.

Do not publish a release or submit to a store unless the user explicitly authorizes that external action.

## Screen-by-screen completion inventory

Derive the exact list from current navigation and features, then ensure the TV implementation/report accounts for all of these categories:

- Cold start/splash/loading and restore active session.
- Home/discovery/recommendation shelves.
- Search entry, results, filters/categories, and voice availability.
- Album detail.
- Artist detail.
- Playlist detail.
- Song context actions.
- User library, liked/favorites, history/recent, and any account collections.
- Local music collection and permissions.
- Downloads, download progress/failure/actions, offline playback.
- Queue and reorder/remove/play-next actions.
- Now Playing.
- Synchronized lyrics/manual lyric browsing.
- Animated canvas/static artwork fallback.
- Module source list, add/edit/test/health check/remove.
- YouTube/Google account status and sign-in/logout.
- Discord, Last.fm, and ListenBrainz connection flows.
- Audio/network/download/playback/appearance settings.
- Sleep timer, speed, skip silence, crossfade, Automix, quality and stats.
- Equalizer intent availability.
- About, GPL license, notices, support links, version/build diagnostics.
- All empty, loading, permission-denied, offline, provider-error, and retry states.

If current code contains additional reachable screens, add them. If one listed feature does not exist, label it not applicable rather than inventing it.

## Detailed quality gates

The work is complete only when all applicable release-blocking gates pass or are explicitly reported as external blockers.

### Gate A — Installation and discovery

- Universal artifact installs on phone and TV without manifest conflicts.
- Phone launcher opens phone UI; TV launcher opens TV UI.
- TV banner/icon render correctly and include readable app identity.
- App is discoverable as a TV app without requiring touch hardware.
- Orientation remains landscape throughout TV activities/dialogs/auth surfaces unless an external provider surface imposes a documented limitation.

### Gate B — Remote completeness

- A five-way D-pad and Back can reach and operate 100% of app-owned TV functions.
- No required swipe, drag, touch, long press, hidden hover, or mobile companion.
- Every screen has valid initial/restored focus and no traps.
- Media keys work in foreground/background.

### Gate C — Presentation

- No clipped text/actions at 720p, 1080p, or 4K.
- Safe margins, readable type, strong focus, correct artwork ratios, and legible backgrounds.
- No phone bottom bar, phone bottom sheet, portrait dialog, or narrow stretched phone screen in the TV route.
- Low-RAM/reduced-motion fallback remains attractive and functional.

### Gate D — Playback/system behavior

- One MediaSession controls one player/service.
- Home/system Now Playing and notification controls show correct metadata and reopen the correct activity.
- Audio continues under Ambient Mode/background and stops/cleans up correctly.
- Audio focus, noisy route, transport, queue, lyrics, lossless/fallback, downloads, crossfade, and Automix show no TV regression in tested configurations.

### Gate E — Native/release compliance

- Production artifacts include proven arm32 and arm64 support.
- All packaged native objects pass 16 KB alignment checks.
- App runs native-dependent features on a 16 KB system.
- APK and AAB build; AAB configuration reports 16 KB alignment.
- Release workflow does not leak secrets and preserves signing/update identity.

### Gate F — Accessibility/resilience

- TalkBack traversal works on Android TV; VoiceView status is recorded for Fire TV.
- Labels, roles, ranges, state, contrast, font scaling, RTL, errors, and reduced motion are addressed.
- Offline, low memory, missing capabilities, process recreation, unavailable providers, and permission denial recover without crash or dead-end focus.

### Gate G — Evidence and honesty

- Tests, device runs, commands, artifacts, and failures are documented exactly.
- No test is called passed if it was skipped, unavailable, or inferred.
- Known limitations have reproducible steps, user impact, and recommended next action.
- `git diff --check` and relevant builds/tests pass; no unrelated changes or secrets are present.

## Required documentation deliverables

Keep documentation concise but auditable:

1. `docs/TV_IMPLEMENTATION_REPORT.md`
   - Baseline/audit, architecture, significant changes by file, dependency decisions, compatibility/fallback behavior, security notes, performance evidence, and unresolved issues.
2. `docs/TV_COMPATIBILITY.md`
   - Android TV/Google TV/Fire TV/non-Android scope, API/ABI/device matrix, tested vs expected behavior, and platform limitations.
3. `docs/TV_TEST_MATRIX.md`
   - Commands, automation results, emulator/physical results, screenshots/evidence references, failures, skipped tests, and manual signoff rows.
4. `docs/TV_RELEASE_CHECKLIST.md`
   - Store/distribution, AAB, assets, quality tiers, native/page-size, privacy/license, and physical-device gates.

Update the main README with a short Android TV/Google TV support section only after the evidence justifies it. Use careful wording such as “Android TV/Google TV support” and tested device/API details; never say “all smart TVs.”

## Official sources of truth

Refresh any time-sensitive details from primary sources before finalizing. Prefer official Android, AndroidX, Media3, Google AI/Antigravity, Amazon, and upstream dependency documentation over blogs, snippets, or search summaries. If official current guidance conflicts with this prompt, follow the official guidance and document the change.

### Antigravity and Gemini execution

- Gemini latest model documentation: `https://ai.google.dev/gemini-api/docs/latest-model`
- Gemini prompting strategies: `https://ai.google.dev/gemini-api/docs/prompting-strategies`
- Gemini 3 developer guide: `https://ai.google.dev/gemini-api/docs/gemini-3`
- Antigravity getting started and `/goal`: `https://antigravity.google/docs/getting-started/`
- Antigravity rules/workflows: `https://antigravity.google/docs/rules-workflows/`
- Antigravity subagents: `https://antigravity.google/docs/subagents/`

If Antigravity subagents are available, use them for bounded parallel audits such as (a) manifest/native/CI, (b) TV UI/focus/accessibility, and (c) Media3/playback/testing. The primary agent must inspect their evidence, reconcile conflicts, and own every final edit. Do not let subagent summaries substitute for reading critical files.

### Android TV application fundamentals

- Create and run a TV app: `https://developer.android.com/training/tv/get-started/create`
- TV hardware differences: `https://developer.android.com/training/tv/get-started/hardware`
- TV controllers: `https://developer.android.com/training/tv/get-started/controllers`
- TV navigation: `https://developer.android.com/training/tv/get-started/navigation`
- TV app quality guidelines: `https://developer.android.com/docs/quality-guidelines/tv-app-quality`
- TV publishing checklist: `https://developer.android.com/training/tv/publishing/checklist`
- TV distribution: `https://developer.android.com/training/tv/publishing/distribute`
- TV design overview: `https://developer.android.com/design/ui/tv`
- TV layouts: `https://developer.android.com/design/ui/tv/guides/styles/layouts`
- TV focus system: `https://developer.android.com/design/ui/tv/guides/styles/focus-system`
- TV navigation drawer: `https://developer.android.com/design/ui/tv/guides/components/navigation-drawer`
- TV icon guidance: `https://developer.android.com/design/ui/tv/guides/system/tv-app-icon-guidelines`

### Compose for TV and focus

- Compose for TV playback/setup: `https://developer.android.com/training/tv/playback/compose`
- Compose for TV layouts: `https://developer.android.com/training/tv/playback/compose/layouts`
- Compose for TV catalog browser: `https://developer.android.com/training/tv/playback/compose/browse`
- Compose for TV lazy lists: `https://developer.android.com/training/tv/playback/compose/lists`
- Change Compose focus behavior: `https://developer.android.com/develop/ui/compose/touch-input/focus/change-focus-behavior`
- Change Compose focus traversal: `https://developer.android.com/develop/ui/compose/touch-input/focus/change-focus-traversal-order`
- Official Android TV samples: `https://github.com/android/tv-samples`
- Universal Android Music Player sample/reference: `https://github.com/android/uamp`

### Media, playback, memory, and native packaging

- Media3 background playback: `https://developer.android.com/media/media3/session/background-playback`
- MediaSession playback controls: `https://developer.android.com/media/media3/session/control-playback`
- Connect a MediaController: `https://developer.android.com/media/media3/session/connect-to-media-app`
- Serve content with MediaLibraryService: `https://developer.android.com/media/media3/session/serve-content`
- TV Now Playing: `https://developer.android.com/training/tv/playback/now-playing`
- TV playback controls: `https://developer.android.com/training/tv/playback/controls`
- TV audio capabilities/routes: `https://developer.android.com/training/tv/playback/audio-capabilities`
- Audio focus: `https://developer.android.com/media/optimize/audio-focus`
- TV memory optimization: `https://developer.android.com/training/tv/playback/memory`
- TV Ambient Mode: `https://developer.android.com/training/tv/playback/ambient-mode`
- 16 KB page-size support: `https://developer.android.com/guide/practices/page-sizes`
- Baseline Profiles: `https://developer.android.com/topic/performance/baselineprofiles/create-baselineprofile`
- Macrobenchmark overview: `https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview`

### Accessibility and Fire TV

- TalkBack support on TV: `https://developer.android.com/training/tv/accessibility/talkback-support`
- Evaluate TalkBack on TV: `https://developer.android.com/training/tv/accessibility/talkback`
- TV custom-view accessibility: `https://developer.android.com/training/tv/accessibility/custom-views`
- Amazon Fire TV differences from Android TV: `https://developer.amazon.com/docs/fire-tv/differences-from-android-tv-development.html`

Use upstream official documentation/release artifacts for ONNX Runtime, QuickJS-kt, the Android NDK, Coil, Haze, and any other native/rendering dependency before changing versions or claiming ABI support.

## Decision rules for ambiguous implementation details

- Prefer the smallest architecture that passes all gates and preserves phone behavior.
- Prefer shared state/actions and separate presentation over duplicated features.
- Prefer stable official APIs over experimental APIs unless the stable path cannot satisfy focus/accessibility; document every experimental opt-in.
- Prefer system components for keyboard, voice, permissions, media controls, and volume.
- Prefer explicit, discoverable TV actions over gesture parity.
- Prefer capability detection over manufacturer/model checks.
- Prefer measurable fallbacks over crashing or silently disabling a feature.
- Prefer keeping a feature with a TV-native interaction; remove/hide only when the platform truly cannot support it.
- Prefer accurate limitations over optimistic marketing.
- Do not wait for user confirmation between ordinary implementation phases.

## Final response contract

When implementation and verification are finished, respond with a concise engineering handoff containing:

1. **Outcome:** what is now implemented and the exact TV scope.
2. **Architecture:** phone vs TV entry points and shared layers.
3. **Key changes:** the most important files/components, not a dump of every line.
4. **Verification:** commands run and counts/results; emulator/physical device/API/ABI/page size; distinguish passed, skipped, and blocked.
5. **Release gates:** Core/Enhanced TV quality status, arm32/arm64 status, 16 KB status, APK/AAB locations, and signing status.
6. **Known limitations:** especially physical Fire TV, provider login, voice, hardware audio-route, or store submission items that could not be exercised.
7. **Next human action:** only genuine hardware/credential/store steps that the agent cannot perform.

Do not include hidden chain-of-thought, generic congratulations, or an enormous prose recap. Link the created documentation and artifacts. Never claim 100% compatibility without the test evidence.

Now execute this goal end-to-end in the current BitChord working tree. Begin with repository/working-tree inspection and baseline verification, then implement, test, repair, and document until every applicable gate is passed or precisely blocked by an external requirement.
