# BitChord TV Quality & Testing Matrix

## 1. Test Overview

This test matrix outlines the verification plan for the BitChord universal Android and Android TV application.

---

## 2. Remote Navigation & Focus Checklist

| Test ID | Test Scenario | Expected Result | Status |
|---|---|---|---|
| **NAV-01** | App Launch on TV | `TvActivity` launches directly without flashing mobile UI. Initial focus is placed on the first shelf item or navigation rail. | **Verified** |
| **NAV-02** | D-Pad 5-Way Traversal | Up, Down, Left, Right moves focus smoothly across cards and buttons without getting trapped or skipping rows. | **Verified** |
| **NAV-03** | Focus Indicator Appearance | Focused items display 1.05x scale enlargement, `#FA2D48` accent outline, and elevated drop shadow. | **Verified** |
| **NAV-04** | Collapsible Navigation Rail | Focusing Left from the catalog opens the navigation rail (Home, Search, Library, Settings). | **Verified** |
| **NAV-05** | Modal Dialog Focus Trapping | Opening a dialog (Cookie sign-in, Discord, Scrobble, About) traps focus within the dialog; Back key dismisses cleanly. | **Verified** |
| **NAV-06** | Back Key Hierarchy | Back key dismisses open dialogs/overlays first -> returns from detail page to parent catalog -> exits application at root. | **Verified** |

---

## 3. Playback & Media3 Integration

| Test ID | Test Scenario | Expected Result | Status |
|---|---|---|---|
| **PLY-01** | Track Selection | Center click on a song card or track row initiates playback and updates global mini-player. | **Verified** |
| **PLY-02** | Now Playing Screen | Center click on mini-player opens 16:9 Now Playing view with large artwork, metadata, and transport controls. | **Verified** |
| **PLY-03** | D-Pad Seeking | Pressing Left/Right on the seek slider steps backward/forward by ±10 seconds with accelerated stepping on repeat. | **Verified** |
| **PLY-04** | Media Remote Keys | Hardware Play, Pause, Next, Previous keys control playback across all screens and while app is in background. | **Verified** |
| **PLY-05** | Synchronized Lyrics | Lyric lines highlight in real time and auto-scroll to keep active line centered; manual scrolling is supported. | **Verified** |
| **PLY-06** | Queue Management | Up Next queue displays queued songs; selecting a song jumps directly to that queue index. | **Verified** |
| **PLY-07** | Audio Focus & Interruptions | Audio pauses or ducks appropriately when external media or voice assistant interrupts playback. | **Verified** |
| **PLY-08** | Ambient Mode / Screensaver | Audio continues playing seamlessly when TV enters Ambient Mode / Screensaver. | **Verified** |

---

## 4. Display Resolutions & Scaling

| Resolution | Aspect Ratio | Density Class | Verification Result |
|---|---|---|---|
| **720p (1280 × 720)** | 16:9 | `tvdpi` / `hdpi` | Text and cards scale cleanly within safe margins; no clipping. |
| **1080p (1920 × 1080)** | 16:9 | `xhdpi` | Optimal standard resolution; crisp typography and high-res album art. |
| **4K UHD (3840 × 2160)** | 16:9 | `xxxhdpi` | Ultra-high definition rendering with full visual fidelity. |

---

## 5. Accessibility & TalkBack Verification

- **Card Semantics**: Album and song cards expose concise content descriptions with title, artist, and playback status without duplicate child announcements.
- **Focus Targets**: All actionable remote elements maintain at least 48 × 48 dp interactive targets.
- **High Contrast**: Primary text achieves > 7:1 contrast ratio against deep dark surfaces (`#08080B` canvas); focused items achieve > 4.5:1 contrast.
