<div align="center">

<br/>
<br/>

<img src="Banner.png" alt="BitChord TV Banner" width="100%" />

# BitChord TV

### Universal YouTube Music Client for Android & Android TV / Google TV

<br/>

[![Latest release](https://img.shields.io/badge/Release-v0.01_TV_Edition-FA2D48?style=for-the-badge&labelColor=0d1117)](https://github.com/nimalanrao/bitchordTV/releases)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue?style=for-the-badge&labelColor=0d1117)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android_%7C_Android_TV-4CAF50?style=for-the-badge&labelColor=0d1117)](#tv-compatibility)
[![Lead](https://img.shields.io/badge/TV_Lead-Nyxcore-purple?style=for-the-badge&labelColor=0d1117)](#credits--authors)

<br/>

[**TV Experience**](#android-tv-features) · [**Cinematic Player & Lyrics**](#cinematic-player--synchronized-lyrics) · [**Mobile Features**](#mobile-features) · [**Remote Controls**](#navigation--remote-controls) · [**Compatibility**](#tv-compatibility) · [**Credits**](#credits--authors)

</div>

> [!WARNING]
> BitChord is an independent open-source project and is not affiliated with, endorsed by, or connected to YouTube, Google LLC, or Apple in any way.

---

## 🌟 Overview

**BitChord** is a premium universal Android music streaming application inspired by Apple Music's design language. It brings lossless audio streaming, intelligent DJ transitions, synchronized lyrics, and modern dark luxury aesthetics to both **smartphones** and **Android TV / Google TV** screens from a single unified application package.

---

## 📺 Android TV & Google TV Experience

BitChord features a dedicated **remote-first television interface** built from the ground up by **Nyxcore** for living rooms and home theater setups:

- **100% 5-Way D-Pad Remote Navigation**: Intuitive spatial focus system designed for TV remotes, gamepads, and keyboards.
- **Dedicated TV Launcher Channel**: Includes 16:9 bitmap banners (320 × 180 px) ensuring the app appears directly in the primary **"Apps"** home screen row across Google TV, Android TV OS, and Fire TV.
- **10-Foot 16:9 Landscape Layouts**: Optimized for 720p, 1080p, and 4K displays with safe-margin protection against overscan.
- **Collapsible Navigation Rail**: Fast, remote-friendly access to Home, Search, Library, and Settings.
- **Television Search with System IME**: Search songs, albums, artists, and playlists with remote input and category filter chips.
- **QR Code Phone-to-TV Sign In**: Direct local QR pairing — scan with your phone on the same Wi-Fi to link your Google / YouTube Music session cookie without remote typing.
- **Ambient Mode & Background Playback**: Music continues playing uninterrupted during screensaver and background transitions.
- **Universal MediaSession Integration**: Home-screen media controls and voice assistant playback commands.

---

## 🎬 Cinematic Player & Synchronized Lyrics

The TV player provides a full-bleed, high-contrast living room listening experience:

- **Full-Bleed 16:9 Artwork & Animated Canvas**: Edge-to-edge album visuals with dynamic darkening and gradient scrims.
- **Real-Time Synchronized Lyrics**:
  - Word-by-word and line-by-line real-time highlighting.
  - Smooth auto-follow scrolling synchronized to Media3 playhead position.
  - D-pad Up/Down manual browsing mode with a floating **"Return to current line"** action.
  - Instant seek snapping and track change synchronization.
- **Thin Progress Timeline**: Subtle resting scrubber that expands upon focus, offering ±10-second D-pad stepping with repeat acceleration.
- **Central Play/Pause Anchor**: Prominent circular transport button flanked by Favorite, Shuffle, Previous, Next, and Queue.
- **Smart Controls Visibility**: Auto-hides controls after idle playback to showcase full-screen visuals, instantly waking on any remote interaction without eating keypresses.

---

## 📱 Mobile Features

The mobile phone experience retains 100% of BitChord's signature polish:

- **Hi-Res Lossless Audio**: Stream FLAC/ALAC from pluggable module sources with YouTube Music fallback.
- **Gapless Playback & Crossfade**: Smooth 0–12s audio transitions between tracks.
- **Automix [Beta]**: On-device AI/DSP beat-matching and tempo transitions powered by ONNX Runtime.
- **Synchronized Lyrics**: Word- and syllable-level highlighting across multiple sources.
- **Offline Downloads & Local Library**: Save tracks with embedded tags; browse local device audio.
- **Frosted Glass Aesthetic**: Modern translucent bars and album-derived color palettes.
- **Rich Integrations**: Discord Rich Presence, Last.fm, and ListenBrainz scrobbling.

---

## 🧭 Navigation & Remote Controls

| Input | TV Action |
|---|---|
| **D-Pad Up / Down / Left / Right** | Move visual focus across cards, buttons, shelves, and lyrics |
| **D-Pad Center / Enter** | Select focused item / Play track / Toggle playback |
| **D-Pad Left / Right (on Player Seekbar)** | Step backward / forward by ±10 seconds |
| **D-Pad Up / Down (in Lyrics Mode)** | Manually browse lyrics (pauses auto-scroll) |
| **Media Play / Pause / Next / Prev** | Global playback control regardless of active screen |
| **Back Button** | Close dialogs/lyrics -> return to previous catalog -> exit |

---

## 🌐 TV Compatibility

| Platform | Target Devices | Status |
|---|---|---|
| **Google TV** | Chromecast with Google TV, Sony Bravia, TCL, Hisense, Philips Google TVs | **Full Support** |
| **Android TV OS** | NVIDIA Shield TV, Xiaomi Mi Box, Mecool, generic Android TV boxes | **Full Support** |
| **Amazon Fire TV** | Fire TV Stick 4K / Max, Fire TV Cube, Fire OS 7+ televisions | **Compatible** |
| **Non-Android TVs** | Samsung Tizen, LG webOS, Roku OS, Apple tvOS | *Out of Scope* |

---

## 📚 Technical Documentation

Explore the complete architecture and release documentation in [`docs/`](docs/):

- 📄 [**TV Implementation Report**](docs/TV_IMPLEMENTATION_REPORT.md) – Architecture, security analysis, and design system decisions.
- 📄 [**TV Setup, Themes & Keyboard Report**](docs/TV_SETUP_THEMES_REPORT.md) – Onboarding flow, visual themes, and built-in remote keyboard.
- 📄 [**TV Performance & 120 Hz Report**](docs/TV_PERFORMANCE_REPORT.md) – Display capability subsystem, frame pacing, and render optimizations.
- 📄 [**TV Compatibility Matrix**](docs/TV_COMPATIBILITY.md) – Device matrix, ABI details, and 16 KB page-size compliance.
- 📄 [**TV Quality & Test Matrix**](docs/TV_TEST_MATRIX.md) – Remote traversal verification, display scaling, and playback checks.
- 📄 [**TV Release Checklist**](docs/TV_RELEASE_CHECKLIST.md) – Store distribution and packaging guide.

---

## 👥 Credits & Authors

- **Nyxcore**: TV Platform Lead, Android TV / Google TV UI/UX Architecture, Remote Focus Engine, and TV Synchronized Lyrics implementation.
- **Kushagra Singh**: Original BitChord mobile application creator and core audio engine.

---

## ⚖️ License & Disclaimer

BitChord is free and open-source software licensed under the **GNU General Public License v3.0 (GPLv3)**. See the [LICENSE](LICENSE) file for complete details.

* **No Media Hosting:** BitChord does not host or store copyrighted audio. It functions as a client for public, user-authenticated, or local device media.
* **API Usage:** Created for research and educational purposes. Users are responsible for complying with relevant local terms and copyright laws.
