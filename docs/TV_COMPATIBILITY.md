# BitChord TV Compatibility Matrix & Platform Scope

## 1. Supported Platform Scope

| Platform Category | Target Environment | Status | Requirements / Notes |
|---|---|---|---|
| **Android TV OS** | Android TV 8.0 (API 26) through Android 16 (API 36) | **Full Support (In-Scope)** | Standard Leanback Launcher, D-pad remote, Media3 service. |
| **Google TV** | Google TV on Chromecast, Sony, TCL, Hisense, Philips | **Full Support (In-Scope)** | High-contrast 10-foot UI, TV banner, Google TV Home integration. |
| **Amazon Fire TV** | Fire OS 7 (API 28, Android 9) & Fire OS 8 (API 30, Android 11) | **Compatible (In-Scope)** | Runs without Google Play Services dependencies; D-pad remote support. |
| **Android Set-Top Boxes** | Shield TV, Mi Box, Formuler, Mecool, generic STBs | **Full Support (In-Scope)** | Ethernet and Wi-Fi unmetered connection support. |
| **Samsung Tizen OS** | Tizen 4.0 - 7.0+ | *Out of Scope* | Non-Android OS; requires separate web app or HTML5 wrapper. |
| **LG webOS** | webOS 4.0 - 24+ | *Out of Scope* | Non-Android OS; requires separate webOS package. |
| **Roku OS** | Roku Streaming Stick / Ultra / TVs | *Out of Scope* | Non-Android OS; requires BrightScript / SceneGraph. |
| **Apple tvOS** | Apple TV 4K / HD | *Out of Scope* | Non-Android OS; requires Swift / SwiftUI application. |

---

## 2. Hardware & Architecture Specifications

### 2.1 API Level Support
- **Minimum SDK**: `API 26` (Android 8.0 Oreo)
- **Target SDK**: `API 36` (Android 16)
- **Compile SDK**: `API 36`

### 2.2 CPU Architectures & Native ABI Support
- **`arm64-v8a`**: 64-bit ARM (Standard for modern Google TV and high-end Android TV devices).
- **`armeabi-v7a`**: 32-bit ARM (Standard for budget Android TVs, streaming sticks, and older Fire TV devices).
- **`x86_64`**: 64-bit x86 (Official Android TV / Google TV development emulators).

### 2.3 16 KB Page-Size Compliance
- NDK build flags and CMake configurations ensure that project-compiled shared libraries (`.so`) align on 16 KB boundaries (`2**14` byte alignment), satisfying Android 15+ 16 KB memory architecture requirements.

---

## 3. Remote Control & Input Matrix

| Input Method | Supported Actions |
|---|---|
| **5-Way D-Pad Remote** | Directional navigation (Up, Down, Left, Right), Select (Center/Enter), Back navigation. |
| **Media Transport Keys** | Play, Pause, Play/Pause toggle, Next Track, Previous Track, Fast Forward, Rewind. |
| **Hardware Keyboards** | Arrow keys for navigation, Enter for selection, Escape/Backspace for back navigation, text typing in search and dialogs. |
| **Gamepads / Controllers** | D-pad / Left Analog stick navigation, A button (Select), B button (Back), Shoulder buttons (Seek/Skip). |
| **Pointer / Air Mouse** | Additive point-and-click support without compromising D-pad focus state. |
| **TalkBack / Screen Readers** | Semantic roles, content descriptions, and structured announcements on all focusable cards and controls. |
