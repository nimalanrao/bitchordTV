/goal

# Add a premium first-run TV setup flow with themes, nickname personalization, and a built-in remote keyboard

Work directly in the currently open BitChord Android repository. Implement a polished TV onboarding/setup experience that lets the user choose a visual theme and enter a local nickname using a complete built-in D-pad keyboard. The nickname and theme must remain editable later from Settings.

This goal assumes the dedicated TV experience, cinematic player, synchronized lyrics, and 60/120 Hz performance work may already exist. Integrate with those systems without redesigning the player, duplicating settings storage, breaking phone behavior, or replacing the existing Media3 playback architecture.

Use Gemini 3.7 Flash with High thinking if available. Inspect the real repository first, implement the feature, run tests, repair regressions, and finish with working code. Do not stop after a plan or mockup. Do not narrate hidden chain-of-thought.

## Required user experience

On the first normal TV launch, show a short cinematic setup flow:

1. **Welcome** — introduce BitChord and begin setup or use safe defaults.
2. **Your name** — ask what BitChord should call the user and open BitChord’s built-in TV keyboard.
3. **Choose your look** — select a theme through large live-preview cards.
4. **Ready** — show the chosen nickname and theme, then enter the TV app.

The entire flow must:

- Work with only D-pad Up/Down/Left/Right, Center/Enter, and Back.
- Work with hardware keyboards and optional system IME as fallbacks.
- Be visually clean, fast, and intentionally animated.
- Respect the TV’s active 60 or Ultra 120 Hz mode without running wasteful loops.
- Persist progress safely through activity recreation and process death.
- Never trap the user or block playback/deep-link recovery.
- Support TalkBack, large text, RTL, 720p, 1080p, and 4K output.
- Remain local and private: the nickname is not an account identity and is never uploaded or logged.

## Scope

Modify only what is needed for:

- TV first-run routing.
- TV setup/onboarding screens.
- Theme definitions, preview, application, and persistence.
- Local nickname entry, validation, persistence, and use in appropriate greetings.
- The built-in in-app TV keyboard.
- Personalization controls in Settings.
- Focus, accessibility, animation, tests, and performance for this feature.

Do not add account registration, marketing pages, tutorials for obvious controls, permission requests unrelated to setup, or a long wizard. Do not implement an Android system-wide `InputMethodService`; this keyboard belongs only inside BitChord.

## Inspect before editing

First inspect:

- Branch, HEAD, working-tree changes, and build/test baseline. Preserve unrelated work.
- `TvActivity`, TV navigation/start destination, splash/loading gate, deep-link routing, MediaSession re-entry, and any existing onboarding.
- Existing Settings architecture, preference keys, DataStore/SharedPreferences/database usage, repositories, ViewModels, migrations, and defaults.
- Existing phone and TV themes, Material theme roots, artwork-driven palette extraction, color tokens, typography, shapes, focus styles, motion, and high-refresh controller.
- Existing user/account names. Do not overwrite or confuse a Google/YouTube profile name with this local nickname.
- Current Compose/TV Material/text-input APIs and versions.
- Existing accessibility semantics, localization, RTL support, and UI tests.

Reuse the established data layer. Official Android guidance places DataStore access in a repository/data layer and exposes it through a ViewModel; do not read or write DataStore directly inside composables.

Run relevant baseline unit tests, a dev debug build, and lint if the environment permits. Record external network/SDK/signing blockers honestly.

## Official sources of truth

Verify current APIs against official documentation before implementing:

- Compose for TV: `https://developer.android.com/training/tv/playback/compose`
- TV navigation and D-pad behavior: `https://developer.android.com/training/tv/get-started/navigation`
- TV design guidance: `https://developer.android.com/design/ui/tv`
- TV color and contrast: `https://developer.android.com/design/ui/tv/guides/foundations/color-on-tv`
- Compose focus: `https://developer.android.com/develop/ui/compose/touch-input/focus`
- Change focus behavior: `https://developer.android.com/develop/ui/compose/touch-input/focus/change-focus-behavior`
- Change focus traversal: `https://developer.android.com/develop/ui/compose/touch-input/focus/change-focus-traversal-order`
- Compose text input and `TextFieldState`: `https://developer.android.com/develop/ui/compose/text/user-input`
- Hardware keyboard commands: `https://developer.android.com/develop/ui/compose/touch-input/keyboard-input/commands`
- DataStore architecture: `https://developer.android.com/topic/libraries/architecture/datastore`
- Compose animation guidance: `https://developer.android.com/develop/ui/compose/animation/quick-guide`
- Test Compose animations: `https://developer.android.com/develop/ui/compose/animation/testing`
- TV accessibility: `https://developer.android.com/training/tv/accessibility`
- TV TalkBack support: `https://developer.android.com/training/tv/accessibility/talkback-support`

Use exact APIs supported by the repository’s selected versions. If state-based `TextFieldState`/`TextFieldBuffer` APIs are available, prefer them for correct selection/edit transactions; otherwise implement an equivalent tested editor without forcing a risky dependency upgrade.

## Architecture

Adapt this separation to existing conventions:

```text
ui/tv/onboarding/
  TvSetupRoute.kt
  TvSetupScreen.kt
  TvWelcomeStep.kt
  TvNicknameStep.kt
  TvThemeStep.kt
  TvSetupCompleteStep.kt
  TvSetupMotion.kt

ui/tv/keyboard/
  TvKeyboard.kt
  TvKeyboardLayout.kt
  TvKeyboardKey.kt
  TvKeyboardFocusGraph.kt
  NicknameEditor.kt

personalization/
  PersonalizationRepository.kt
  PersonalizationPreferences.kt
  AppThemeOption.kt
  NicknamePolicy.kt
```

Do not create duplicate layers when equivalents exist.

Use one source of truth for persisted personalization:

```kotlin
data class PersonalizationPreferences(
    val nickname: String,
    val theme: AppThemeOption,
    val setupVersionCompleted: Int
)
```

Use stable serialized theme IDs, not enum ordinals. Add a migration/default strategy for missing, corrupt, renamed, or removed values.

The setup ViewModel owns a draft:

- Current step.
- Draft nickname and cursor/selection state where appropriate.
- Draft theme.
- Validation state.
- Whether a save is in progress.
- Original values when setup is reopened from Settings.

Persist completed choices atomically. Do not briefly store half-completed setup as final personalization. Preserve the draft using `SavedStateHandle` or equivalent so rotation/activity recreation does not erase input.

## First-launch routing rules

- Fresh TV install: show setup before the normal TV home route.
- The setup gate must wait only for the small local preference read; it must not display the wrong screen and then flash into setup.
- Show a lightweight branded loading frame while preference state resolves.
- `setupVersionCompleted` allows future migrations without repeatedly showing the same onboarding.
- Existing phone users upgrading to TV support must not lose their current theme/settings. Map the current theme to the closest new option.
- Do not force an established user into a blocking wizard during notification/MediaSession/deep-link re-entry. If active playback reopens Now Playing, honor it and surface personalization later as an optional TV prompt.
- **Use defaults** or **Skip for now** must remain available. It selects a safe nickname such as `Listener`, keeps the default theme, marks this setup version handled, and enters the app.
- Back on the first setup step exits through normal system behavior. No exit confirmation.
- Back on later steps goes to the previous setup step and restores its last meaningful focus.
- Completing or skipping setup creates no duplicate activity/task.
- Reopening setup from Settings never logs out accounts, deletes playback history, clears downloads, or resets unrelated preferences.

## Visual direction

Create a premium BitChord TV identity—not a generic mobile onboarding card stretched across a television.

### Overall composition

- Full-screen 16:9 layout with a dark cinematic BitChord background.
- Use subtle layered gradients, soft artwork-inspired color, and restrained depth.
- Keep critical content within TV safe areas, approximately 48 dp horizontally and 24–30 dp vertically at the 960×540 dp baseline.
- Use a two-zone layout where appropriate: copy/status on the left and interactive content/preview on the right.
- Never place the entire flow inside a narrow phone-shaped panel.
- Show a compact `1 of 3`, `2 of 3`, or semantic progress indicator, excluding the final confirmation if that reads better.
- BitChord logo remains visible but does not dominate every step.
- Use high-contrast ten-foot typography. Do not shrink text to fit.

### Motion language

- Step transitions: short fade-through plus restrained horizontal movement that communicates forward/back direction.
- Theme preview transition: crossfade colors and preview tokens without a full-screen flash.
- Focus: quick scale/outline/glow/color response, approximately 100–160 ms.
- Primary content entrance: approximately 180–300 ms, staggered only slightly.
- Use interruptible animations so rapid Back/Continue does not leave overlapping screens.
- Favor draw/graphics-layer animation over layout-changing animation for performance.
- Do not animate full-screen blur radius, large shadows, or endless particles.
- Respect system animator scale and provide reduced-motion behavior: near-instant fades with no large translation/scale.
- At 120 Hz, animations should receive frames from the existing refresh-rate system naturally. Never create a `delay(8)` loop or redraw static setup UI continuously.

## Step 1 — Welcome

Design a clean welcome screen:

- BitChord logo/icon.
- Heading such as **Make BitChord yours**.
- One short sentence: choose how the app looks and what it calls the user.
- Primary action: **Start setup**.
- Secondary action: **Use defaults**.
- Optional minimal visual preview of artwork, lyrics, and player controls using real design tokens—not fake playable controls.

Initial focus must be **Start setup**. Both actions are reachable by D-pad and have clear TalkBack descriptions.

## Step 2 — Nickname

Ask: **What should BitChord call you?**

- Explain locally and briefly: “Used for greetings on this TV. You can change it anytime in Settings.”
- Display a large editable nickname field with visible cursor/selection, character count, validation message, and current draft.
- The built-in keyboard is visible on the same screen or opens as a full-screen TV overlay without a mobile keyboard popup.
- The text field, keyboard, Continue, and Back must fit without clipping at 720p and large font scale.
- Continue is enabled for a valid nickname and disabled with an explanation otherwise.
- Allow **Skip** to use `Listener` or the existing nickname.
- When returning from theme selection, preserve the nickname and last focus.

### Nickname policy

- Allow 1–24 Unicode grapheme clusters after trimming leading/trailing whitespace.
- Allow internal spaces, letters, combining marks, numbers, emoji, apostrophes, periods, hyphens, and underscores.
- Reject line breaks, null/control characters, and input consisting only of whitespace.
- Collapse or reject unreasonable repeated whitespace according to one documented rule.
- Do not limit by UTF-16 `String.length`; emoji and combined characters must not count as multiple visible characters.
- Backspace and cursor movement must never split surrogate pairs, combining sequences, flags, or emoji modifier sequences. Use Android ICU `BreakIterator.getCharacterInstance()` or another tested grapheme-boundary implementation available at the project’s min SDK.
- Do not censor words or send the nickname to a moderation/network service.
- Escape/render text safely. The nickname is plain display text, never HTML/SQL/file path/URL code.
- Never log analytics containing the nickname.

## Built-in TV keyboard

The built-in keyboard is the default nickname input method and must be a real reusable component, not a screenshot or collection of unreliable click boxes.

### Required layouts

Provide at least these pages:

1. **Letters** — QWERTY with lowercase/uppercase through Shift/Caps.
2. **Numbers and symbols** — digits and common nickname punctuation.
3. **Emoji** — a small curated page of common emoji suitable for nicknames, with Unicode-safe insertion.

Suggested letter geometry, adapted to safe TV spacing:

```text
1  2  3  4  5  6  7  8  9  0
Q  W  E  R  T  Y  U  I  O  P
 A  S  D  F  G  H  J  K  L
Shift  Z  X  C  V  B  N  M  Backspace
ABC/123  Emoji  Cursor Left  Space  Cursor Right  Clear  Done
```

Use lowercase labels when shift is off if that improves clarity. Do not duplicate number keys unnecessarily if a cleaner letter layout and dedicated symbol page works better.

### Editing behavior

- Character key inserts at the current selection and replaces selected text.
- Space inserts one space, subject to nickname whitespace policy.
- Backspace removes selection, otherwise the full grapheme immediately before the cursor.
- Optional Forward Delete removes selection or the next full grapheme.
- Cursor Left/Right moves by grapheme boundary, not UTF-16 code unit.
- Clear asks for confirmation only when accidental total loss would be frustrating; Undo Clear may be smoother than a modal confirmation.
- Shift affects the next letter then resets.
- Caps Lock remains active and has a persistent selected state. A clear remote gesture/button toggles it; do not require double-tap timing as the only route.
- `ABC`, `123`, and Emoji switch pages while preserving text and cursor.
- Done validates, closes keyboard, and returns focus to Continue or the field if invalid.
- Back dismisses the keyboard/returns to the step while preserving the draft. A second Back follows normal setup navigation.
- Repeated Backspace from a held remote key deletes at a controlled rate after an initial delay and stops instantly on key-up/focus loss/lifecycle pause.
- Do not double-insert on key down plus key up. Trigger text actions once on the correct event.

Use `TextFieldState.edit` and `TextFieldBuffer` insert/replace/delete/selection APIs when available. Keep all edits transactional. Do not mutate text from several competing state sources.

### Focus behavior

Build a deterministic keyboard focus graph using stable key IDs:

- Every key is reachable.
- Left/Right remains in the row and does not unpredictably jump into the text field or buttons.
- Up/Down moves to the geometrically nearest sensible key, including rows with offsets and wide Space/Done keys.
- Edge keys do not wrap unless the behavior is deliberate, consistent, and tested.
- Switching Shift or keyboard pages restores the equivalent key when possible; otherwise use a documented fallback.
- Opening the keyboard creates exactly one clear initial focus. Use `Q`/the first key for a new name or restore the previous keyboard key while editing.
- Done and Backspace are easily reachable without crossing the full grid repeatedly.
- Focus never disappears after key-page changes, invalid input, animation, recomposition, process restore, or large font scaling.
- Wide keys must not confuse spatial focus. Use `focusProperties`/a focus graph where Compose’s default 2D search fails.
- TalkBack focus and D-pad focus operate the same actionable keys. Do not build the keyboard as one custom Canvas with no accessibility nodes.

### Hardware and system input

- A connected hardware keyboard must type directly into the same editor.
- Correctly handle printable Unicode, Space, Backspace/Delete, Shift/Caps Lock, cursor arrows, Home/End where supported, Enter/Done, Escape/Back, Ctrl+A, and ordinary selection behavior.
- Do not consume D-pad navigation, media keys, Home, volume, or unrelated shortcuts as characters.
- Avoid handling the same event in the Activity and composable, causing duplicate input.
- Provide a secondary **Use system keyboard** action for voice input, non-Latin languages, advanced IMEs, and accessibility. The built-in keyboard remains fully usable without it.
- When invoking the system IME, use a real editable text component/input connection and verified focus chaining. Return to the same draft and correct focus when the IME closes.
- Do not declare microphone hardware required. Voice input availability is controlled by the system IME/device.
- If suppressing automatic IME display for the built-in keyboard, use the exact supported Compose/platform API rather than immediately hiding the IME after every focus event and creating flicker.

## Step 3 — Theme selection

Implement at least three polished themes:

### Dynamic Artwork — default/recommended

- Dark neutral base.
- Accent and supporting tones derived safely from current artwork when available.
- Contrast correction so text/focus never becomes unreadable.
- Stable fallback palette before artwork loads or when colors fail.

### Midnight

- Deep navy/charcoal background.
- Cool blue/violet accent.
- Soft elevated surfaces and high-contrast white typography.
- Premium rather than neon/gaming appearance.

### Pure Black

- True or near-black main background for OLED displays.
- Minimal translucent surfaces.
- White/gray typography with one restrained BitChord accent.
- Avoid black-crush: focused boundaries and secondary text must remain visible on different TVs.

Do not create three themes by changing only one accent hex value. Each must define a coherent semantic token set:

- Background and elevated background.
- Surface/container and overlay.
- Primary/secondary text.
- Accent and on-accent.
- Focus ring/glow.
- Selected, pressed, disabled, error, success, divider, scrim, progress, and lyric colors.
- Any blur/gradient policy.

Use a stable `AppThemeOption` and one theme resolver. No color conditionals scattered throughout screens.

### Theme chooser UI

- Show large horizontal cards with a real miniature preview: background, album square, song text, progress line, focused play control, and active lyric line.
- The card itself is one focus target; preview children are decorative and not separately focusable.
- Focus previews the theme live across the setup background without persisting immediately.
- Center selects it and exposes a persistent selected indicator.
- Continue persists only when finishing the setup; Back restores the previous draft.
- Theme changes use a smooth crossfade, but rapid focus movement cancels stale transitions rather than queuing them.
- All theme names and descriptions are localized resources.
- Test each theme with bright/dark album artwork, SDR/HDR display behavior where possible, large text, TalkBack, and color-vision considerations.

## Step 4 — Ready

- Apply the chosen theme fully.
- Heading: **You’re all set, {nickname}** with correct localization rather than unsafe string concatenation.
- Show a concise summary: selected theme and where to change personalization later.
- Primary action: **Start listening**.
- Optional secondary action: **Change choices**, returning to the relevant step without losing the draft.
- On completion, perform one atomic preference write, update the in-memory preference flow immediately, mark the setup version complete, and navigate to the TV home route without recreating the activity or flashing the old theme.
- Initial focus is Start listening.

## Settings integration

Add a **Personalization** section to the TV Settings screen:

- **Nickname** — shows the current nickname and opens the same nickname editor/built-in keyboard.
- **Theme** — shows the current theme and opens the same live theme picker.
- **Run setup again** — reopens setup using current values as the draft; does not reset accounts or other settings.
- Optional **Reset personalization** — restores `Listener` plus Dynamic Artwork after a clear confirmation.

Requirements:

- Reuse the exact keyboard, nickname policy, theme previews, and repository. No second implementation.
- Theme can preview live, but Cancel must restore the original theme reliably.
- Nickname Save is explicit and disabled while invalid. Cancel restores the original.
- After Save/Cancel, focus returns to the invoking Settings row.
- Changes propagate through one preference Flow to all appropriate TV surfaces without activity restart.
- Use the nickname only for natural greetings such as Home/setup. Do not inject it into every title, notification, MediaSession metadata, lyrics, or player controls.
- If phone UI is meant to share personalization, integrate through the same repository after regression testing. Otherwise scope TV rendering explicitly and do not disturb the phone theme.

## Theme loading without flashes

- Resolve the persisted theme before drawing the main TV root whenever possible.
- Use a small branded launch/loading frame matching the stored/default background while DataStore loads.
- Never render Light/default Material for one frame before switching to a dark theme.
- Dynamic Artwork must have a deterministic fallback before media artwork exists.
- Do not block startup on network, account, artwork, or palette extraction.
- Corrupt/unknown theme IDs fall back to Dynamic Artwork and repair safely.

## Performance and animation requirements

- Keep setup state immutable and small.
- Do not recompose the entire keyboard for cursor blinking or one key’s focus animation.
- Use stable keyboard-key IDs and remember immutable layout definitions.
- Use render-layer focus scale/alpha rather than resizing and relaying out the whole grid.
- Avoid creating a `FocusRequester` array on every recomposition.
- Cache preview brushes, shapes, icons, and text styles with correct keys.
- Do not calculate theme contrast/palette on the main thread during focus movement.
- Theme previews use token data, not three independent nested app themes with expensive trees.
- Cursor blink stops when editor is unfocused, activity pauses, or reduced motion requires it.
- Key repeat uses one cancellable job and no leaking coroutine.
- Animations must meet the existing 16.67 ms 60 Hz and 8.33 ms 120 Hz targets on tested hardware.
- Static setup screens do not redraw continuously.
- Add the setup and keyboard journey to the app Baseline Profile/Macrobenchmark if that infrastructure exists.

## Accessibility and internationalization

- Every screen has one clear heading and meaningful initial focus.
- Step/progress information has concise semantics.
- Every keyboard key exposes label, role, enabled/selected state, and action. Shift/Caps announces its current state.
- Symbol and emoji keys have human-readable descriptions, not raw code-point names where confusing.
- Do not make both theme-card parent and decorative preview children focusable.
- Nickname field announces current value, cursor/selection state where practical, error, and character limit without repeating after every focus move.
- Maintain strong focus contrast in every theme; do not rely only on color.
- Support TalkBack operation from welcome through completion and Settings editing.
- Test without looking at the screen, as current TV accessibility guidance recommends.
- Support text scaling without clipping keyboard rows or primary actions. Adapt spacing/layout rather than shrinking below readable sizes.
- Support RTL for setup copy/layout while preserving familiar QWERTY key order unless a true localized keyboard layout is implemented.
- Physical/system keyboard input must accept non-Latin text. The built-in Latin keyboard does not justify claiming every writing system is represented.
- Localize all copy and use format resources for the nickname greeting.
- Respect reduced-motion and high-contrast needs.

## Error and recovery states

Handle:

- DataStore read failure or corrupt value.
- Atomic save failure with retry and no false completion.
- Activity/process recreation on every step.
- App backgrounded while keyboard is open.
- Rapid Continue/Back input during transitions.
- D-pad key held during screen/page transition.
- Empty/invalid/over-limit nickname.
- Emoji/combining sequence deletion and cursor movement.
- Hardware keyboard attached/detached.
- System IME canceled or unavailable.
- Theme preview interrupted by Back/Cancel.
- Existing playback continuing during setup.
- Deep link/MediaSession entry while setup is incomplete.

Never leave an endless spinner, blank screen, duplicate navigation, lost focus, stale preview theme, erased draft, or marked-complete setup after a failed save.

## Required tests

### Data/routing tests

- Fresh install routes to setup.
- Completed setup routes normally.
- Unknown/corrupt setup version and theme ID recover safely.
- Skip stores defaults.
- Completion writes nickname/theme/version atomically.
- Save failure leaves setup open and recoverable.
- Existing-user/deep-link/MediaSession routing does not block active playback.
- Run setup again preserves unrelated data.
- Process recreation restores step and draft.

### Nickname/editor tests

- Insert, replace selection, cursor Left/Right, Backspace, Forward Delete, Clear/Undo, Done, Shift, Caps, page switch, and held Backspace.
- Empty, whitespace-only, leading/trailing spaces, repeated internal spaces, 1 and 24 grapheme limits, and attempted 25th grapheme.
- ASCII, accented letters, combining characters, Tamil/other non-Latin input from hardware/system IME, emoji, skin-tone emoji, flags, family ZWJ emoji, apostrophe, hyphen, underscore, and mixed RTL/LTR.
- Deletion/cursor movement never splits a grapheme.
- No duplicate key-down/key-up insertion.
- Back preserves draft and Done validates.
- Nickname never appears in logs/analytics test hooks.

### Keyboard focus tests

- Initial focus.
- Every directional edge from every key on Letters, Symbols, and Emoji pages.
- Wide Space/Clear/Done actions do not create jumps.
- Page and Shift changes retain valid focus.
- Focus survives text edits, validation, animation, recreation, font scaling, and system IME return.
- TalkBack can reach and activate every key.
- Media/Home/volume keys are not consumed.

### Theme tests

- Each semantic token exists and passes contrast checks for representative surfaces.
- Focus preview changes immediately; selected/persisted theme changes only at the correct commit point.
- Cancel restores original theme.
- Settings edits propagate without activity recreation.
- Unknown theme falls back safely.
- Dynamic theme fallback works without artwork.
- No light-theme startup flash.

### UI/animation/performance tests

- Complete flow at 1280×720, 1920×1080, and 3840×2160.
- Large font/display scale, LTR, RTL, TalkBack, remote, gamepad, and hardware keyboard.
- Deterministic animation tests with Compose’s test clock for step transitions, focus state, and theme crossfade.
- Rapid repeated D-pad and Back/Continue does not duplicate screens/actions.
- Frame timing on 60 and real 120 Hz hardware where available.
- Setup/keyboard does not cause audio interruption while playback continues.

Run repository-equivalent tasks, adapting actual variant names:

```bash
./gradlew test
./gradlew assembleDevDebug
./gradlew lintDevDebug
./gradlew connectedDevDebugAndroidTest
```

Run focused setup/keyboard tests frequently. Do not disable failing tests or suppress lint to finish.

## Prohibited shortcuts

- No fake keyboard image.
- No touch-only input.
- No reliance on long press as the only way to type or change case.
- No single `String` state mutated independently by UI, hardware keyboard, and system IME.
- No UTF-16-character deletion that corrupts emoji/combined text.
- No `InputMethodService` or attempt to become the system keyboard.
- No built-in keyboard that blocks hardware/system multilingual input.
- No raw `onKeyEvent` handler consuming all keys.
- No focus graph based only on accidental screen coordinates.
- No theme implemented as scattered color `if` statements.
- No saving DataStore directly from composables.
- No theme flash, activity restart, or full navigation reset after theme selection.
- No endless particle/gradient animation.
- No claiming every language is covered by a Latin keyboard.
- No logging the nickname.
- No placeholder TODOs, fake previews, disabled tests, or unverified “100% working” claim.

## Definition of done

Complete only when:

- Fresh TV launch shows the polished setup flow exactly once for the current setup version.
- The user can finish or skip entirely with a five-way D-pad.
- Nickname entry works through the built-in keyboard, hardware keyboard, and optional system IME.
- All keyboard keys/actions, selection, grapheme-safe editing, focus paths, repeat behavior, and Back behavior are tested.
- Dynamic Artwork, Midnight, and Pure Black are genuinely distinct complete themes.
- Theme previews are live, smooth, cancellable, and do not persist prematurely.
- Setup completion persists nickname/theme/version atomically.
- Theme and nickname can be edited later in Settings using the same components.
- Theme applies without restart or startup flash.
- Process death, storage error, system IME cancellation, deep link, and active playback recover safely.
- TalkBack, large text, RTL, 720p/1080p/4K, reduced motion, 60 Hz, and available 120 Hz testing are documented honestly.
- Phone UI and existing playback/player/lyrics behavior remain intact.
- Relevant builds, unit tests, lint, and available instrumentation tests pass.

## Final response contract

After implementation, return a concise handoff with:

1. Setup flow and routing behavior.
2. Theme choices and storage architecture.
3. Keyboard layouts, editing capabilities, and multilingual fallback.
4. Settings integration.
5. Files created/modified.
6. Tests/builds and exact results.
7. Devices, resolutions, refresh rates, remote/hardware keyboard, and TalkBack actually tested.
8. Any external hardware or environment blocker.

Do not respond with only a plan. Start by auditing the existing TV start route, theme/settings storage, and Compose input APIs, then implement and validate the complete setup experience.
